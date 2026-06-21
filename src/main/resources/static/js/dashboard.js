function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
}

function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
}

function renderLineChart(canvasId, labels, values) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;

    new Chart(canvas, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Requests',
                data: values,
                fill: false,
                borderColor: 'rgb(173, 70, 255)',
                tension: 0.1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: false,
                    min: 0,
                    ticks: {
                        stepSize: 1
                    }
                }
            }
        }
    });
}