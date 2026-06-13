const baseUrl = "http://localhost:8080";
let stopped = false;

async function solveChallenge(challenge, difficulty, onProgress, isStopped) {
    const target = difficulty;
    let nonce = 0;
    let lastUpdate = performance.now();
    let lastNonce = 0;
    const expectedAttempts = Math.pow(2, difficulty);

    while (true) {
        if (isStopped && isStopped()) {
            return null;
        }
        const input = `${challenge}:${nonce}`;
        const encoder = new TextEncoder();
        const data = encoder.encode(input);
        const hashBuffer = await crypto.subtle.digest('SHA-256', data);
        const hashArray = new Uint8Array(hashBuffer);

        if (hasLeadingZeroBits(hashArray, target)) {
            onProgress(100, 0);
            return nonce;
        }

        const now = performance.now();
        if (now - lastUpdate > 50) {
            const speed = (nonce - lastNonce) / ((now - lastUpdate) / 1000);
            onProgress(Math.min(99, (nonce / expectedAttempts) * 100), speed);
            lastUpdate = now;
            lastNonce = nonce;
        }

        nonce++;
        if (nonce % 100 === 0) await new Promise(r => setTimeout(r, 0));
    }
}

function hasLeadingZeroBits(hash, requiredBits) {
    const fullBytes = Math.floor(requiredBits / 8);
    const remainingBits = requiredBits % 8;

    for (let i = 0; i < fullBytes; i++) {
        if (hash[i] !== 0) return false;
    }

    if (remainingBits > 0) {
        const mask = 0xff << (8 - remainingBits);
        if ((hash[fullBytes] & mask) !== 0) return false;
    }

    return true;
}

function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
}

function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') ?? 'X-XSRF-TOKEN';
}

async function init() {
    const statusText = document.getElementById("pow-status")
    const progressBar = document.getElementById("progress-bar")
    let challenge;
    let solvedJson;

    progressBar.style.width = "0%";
    progressBar.style.backgroundColor = "#9810fa";
    stopped = false;


    statusText.textContent = "requesting challenge..";
    try {
        // get difficulty and prefix from api
        const resp = await fetch(baseUrl + "/api/v1/pow/challenge");
        challenge = await resp.json();
    } catch {
        statusText.textContent = "could not reach server";
        progressBar.style.width = "100%";
        progressBar.style.backgroundColor = "#e10000"
        return;
    }

    if (challenge.prefix == null || challenge.difficulty == null) {
        statusText.textContent = "failed to receive challenge"
        progressBar.style.width = "100%";
        progressBar.style.backgroundColor = "#e10000"
        return;
    }



    const nonce = await solveChallenge(challenge.prefix, challenge.difficulty, (percent, speed) => {
        progressBar.style.width = `${percent}%`;
        document.getElementById('pow-speed').textContent = `${speed.toFixed(0)} H/s`;
        statusText.textContent = `verifying..`;

        if (new Date() > challenge.expiresAt) {
            stopped = true;
        }
    }, () => stopped);

    if (stopped) {
        statusText.textContent = "challenge expired, retrying..";
        await init();
        return
    }

    try {
        const resp = await fetch(baseUrl + "/api/v1/pow/verify", {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [getCsrfHeader()]: getCsrfToken()
            },
            credentials: 'include',
            body: JSON.stringify({
                id: challenge.id,
                nonce: nonce
            })
        })

        solvedJson = await resp.json();
    } catch {
        statusText.textContent = "failed to verify challenge"
        progressBar.style.width = "100%";
        progressBar.style.backgroundColor = "#e10000"
        return;
    }

    if (solvedJson.success == null) {
        statusText.textContent = "failed to verify challenge"
        progressBar.style.width = "100%";
        progressBar.style.backgroundColor = "#e10000"
        return;
    }

    if (solvedJson.success) {
        statusText.textContent = "verified"
        progressBar.style.width = "100%";
        progressBar.style.backgroundColor = "#18e600"
        window.location.reload()
    } else {
        statusText.textContent = "verification failed"
        progressBar.style.width = "100%";
        progressBar.style.backgroundColor = "#e10000"
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    await init()
})