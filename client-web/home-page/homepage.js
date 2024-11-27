releaseDomain(sessionStorage.getItem('domainToRelease'))

// Add click event listener to the "My Domains" button to redirect to the domains page
document.getElementById('my-domains').addEventListener('click', function () {
  window.location.href = './my-domains.html'; // Adjust the path as necessary
});

// Add click event listener to the "My Orders" button to redirect to the orders page
document.getElementById('my-orders').addEventListener('click', function () {
  window.location.href = './orders.html'; // Adjust the path as necessary
});

// Resets the search form by clearing the domain search input field
function resetSearchForm() {
  document.getElementById('domain-search-input').value = '';
}

// Asynchronously searches for a domain based on the input value
async function searchDomain() {
  // Trim and retrieve the domain ID from the input field
  const domainId = document.getElementById('domain-search-input').value.trim();

  // If no domain ID is provided, exit the function
  if (!domainId) {
    return;
  }

  // Retrieve the user's session token from session storage
  const userId = sessionStorage.getItem('sessionToken');

  // Construct the request URL with the domain ID and user ID
  const url = `http://localhost:8080/domains/${encodeURIComponent(domainId)}?userId=${encodeURIComponent(userId)}`;

  try {
    // Perform the GET request to the server to search for the domain
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    // If the response is not OK, throw an error
    if (!response.ok) {
      throw new Error(`Server responded with status: ${response.status}`);
    }

    // Parse the JSON response
    const jsonResponse = await response.json();

    // Handle the response based on the action specified in the JSON
    switch (jsonResponse.action) {
      case "View owner details":
      case "Update your domain":
        // If there are document details, store them in session storage
        if (jsonResponse.document) {
          sessionStorage.setItem('domainDetails', JSON.stringify(jsonResponse.document));
        }
        // Redirect based on the action
        window.location.href = jsonResponse.action === "Update your domain" ? '../renew-domain-page/renew.html' : '../owner-details-page/owner.html';
        break;
      case "Buy domain":
        // If buying a domain, store the domain ID to buy in session storage
        if (jsonResponse.document) {
          sessionStorage.setItem('domainToBuy', domainId);
        }
        // Redirect to the buy domain page
        window.location.href = '../buy-domain-page/buy.html';
        break;
      case "Concurrency error":
        // Redirect to the concurrency error page
        window.location.href = '../concurrency-error-page/error.html';
        break;
      default:
        // Log to console if no specific action is required
        console.log('No specific action required.');
    }
    // Reset the search form after handling the response
    resetSearchForm();
  } catch (error) {
    // Log the error to the console and alert the user
    console.error('Error:', error.message);
    alert('Error searching for domain. Please try again. Debug info: ' + error.message);
  }
}

// Add event listeners once the DOM content is fully loaded
document.addEventListener('DOMContentLoaded', function () {
  // Navigate to the my domains page when the my domains button is clicked
  document.getElementById('my-domains').addEventListener('click', function () {
    window.location.href = '../my-domains-page/domains.html';
  });

  // Navigate to the my orders page when the my orders button is clicked
  document.getElementById('my-orders').addEventListener('click', function () {
    window.location.href = '../my-orders-page/orders.html';
  });

  // Add click event listener to the search icon to initiate domain search
  document.getElementById('search-icon').addEventListener('click', searchDomain);

  // Add keypress event listener to trigger domain search on pressing Enter
  document.getElementById('domain-search-input').addEventListener('keypress', function (event) {
    if (event.key === 'Enter') {
      searchDomain();
    }
  });
});

async function releaseDomain(domainId) {
  try {
    const response = await fetch(`http://localhost:8080/domains/release/${domainId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
      },
    });
  } catch (error) {
    alert('Error:', error.message);
  }
}