document.addEventListener('DOMContentLoaded', async () => {
  const tableBody = document.querySelector('.domain-table tbody');
  const userId = sessionStorage.getItem('sessionToken');

  // Event delegation for hover effect on table rows
  tableBody.addEventListener('mouseenter', event => {
    if (event.target.tagName === 'TR') {
      // Set a timeout to add hover class
      let hoverTimeout = setTimeout(() => {
        event.target.classList.add('hovered');
      }, 400);
      // Store timeout ID for later use
      event.target.dataset.hoverTimeout = hoverTimeout;
    }
  }, true); // Capture phase

  tableBody.addEventListener('mouseleave', event => {
    if (event.target.tagName === 'TR') {
      // Retrieve and clear the hover timeout
      const hoverTimeout = event.target.dataset.hoverTimeout;
      clearTimeout(hoverTimeout);
      event.target.classList.remove('hovered');
    }
  }, true); // Capture phase

  // Fetch and display domains
  try {
    const response = await fetch(`http://localhost:8080/domains?userId=${userId}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`Server responded with status: ${response.status}`);
    }

    const jsonResponse = await response.json();

    // Clear table and populate with new rows
    tableBody.innerHTML = '';
    jsonResponse.forEach(domain => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${domain.domainId}</td>
        <td>${domain.currentDate}</td>
        <td>${domain.expirationDate}</td>
      `;
      tableBody.appendChild(row);

      // Add click event listener to each row
      row.addEventListener('click', () => {
        searchDomain(domain.domainId, userId);
      });
    });

  } catch (error) {
    alert('Failed to fetch domains: ' + error.message);
  }
});

// Function to handle domain search
async function searchDomain(domainId, userId) {
  // Validate input
  if (!domainId || !userId) {
    console.error('Domain ID or User ID is missing.');
    return;
  }

  // Construct request URL
  const url = `http://localhost:8080/domains/${encodeURIComponent(domainId)}?userId=${encodeURIComponent(userId)}`;

  try {
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

    // Handle response action
    switch (jsonResponse.action) {
      case "View owner details":
      case "Update your domain":
        if (jsonResponse.document) {
          sessionStorage.setItem('domainDetails', JSON.stringify(jsonResponse.document));
        }
        window.location.href = jsonResponse.action === "Update your domain" ? '../renew-domain-page/renew.html' : '../owner-details-page/owner.html';
        break;
      case "Buy domain":
        if (jsonResponse.document) {
          sessionStorage.setItem('domainToBuy', domainId);
        }
        window.location.href = '../buy-domain-page/buy.html';
        break;
      default:
        console.log('No specific action required.');
    }
  } catch (error) {
    console.error('Error:', error.message);
    alert('Error searching for domain. Please try again. Debug info: ' + error.message);
  }
}