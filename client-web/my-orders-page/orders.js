document.addEventListener('DOMContentLoaded', async () => {
    const tableBody = document.querySelector('.order-table tbody');

    try {
        // Recupera l'ID dell'utente dalla sessione
        const userId = sessionStorage.getItem('sessionToken');
        if (!userId) {
            throw new Error('User ID not found in session');
        }

        // Costruisce l'URL per la chiamata API, includendo l'userId come parametro di query
        const url = `http://localhost:8080/orders?userId=${userId}`;
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Server responded with status: ${response.status}`);
        }

        const jsonResponse = await response.json();

        tableBody.innerHTML = '';
        jsonResponse.forEach(order => {
            const row = document.createElement('tr');

            row.innerHTML = `
                <td>${order.domainId}</td>
                <td>${order.orderDate}</td>
                <td>${order.price}$/month</td>
                <td>${order.type}</td>
            `;
            tableBody.appendChild(row);
        });

    } catch (error) {
        alert('Failed to fetch orders: ' + error.message);
    }
});

// Definisci la funzione searchOrder se non esiste
function searchOrder(orderId, userId) {
    // Implementa la logica per cercare un ordine specifico
    console.log(`Searching for order ${orderId} for user ${userId}`);
}
