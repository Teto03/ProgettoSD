document.addEventListener('DOMContentLoaded', async function() {
  try {
    // Retrieve and parse domainDetails from sessionStorage to get userId
    const domainDetails = JSON.parse(sessionStorage.getItem('domainDetails'));
    if (!domainDetails || !domainDetails.userId) {
      throw new Error('User ID not found in domain details');
    }

    const domainId = domainDetails.domainId;
    const userId = domainDetails.userId;

    // Make the first letter of the domain uppercase
    const formattedDomainId = domainId.charAt(0).toUpperCase() + domainId.slice(1);

    // Display the formatted domain ID in the .domain element
    const domainElement = document.querySelector('.domain');
    domainElement.textContent = formattedDomainId;

    // Fetch the domain details
    const url = `http://localhost:8080/domains/${encodeURIComponent(domainId)}?userId=${encodeURIComponent(userId)}`;
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

    // Display the expiration date in the .expiration-date element
    const expirationDateElement = document.querySelector('.expiration-date');
    expirationDateElement.textContent = jsonResponse.document.expirationDate;

    // Fetch the owner details
    const ownerUrl = `http://localhost:8080/registrations/${encodeURIComponent(userId)}`;
    const ownerResponse = await fetch(ownerUrl, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!ownerResponse.ok) {
      throw new Error(`Server responded with status: ${ownerResponse.status}`);
    }

    const ownerJsonResponse = await ownerResponse.json();

    // Display the owner's name, surname, and email
    const ownerNameElement = document.querySelector('.name');
    ownerNameElement.textContent = ownerJsonResponse.name;

    const ownerSurnameElement = document.querySelector('.surname');
    ownerSurnameElement.textContent = ownerJsonResponse.surname;

    const ownerEmailElement = document.querySelector('.email');
    ownerEmailElement.textContent = ownerJsonResponse.email;

  } catch (error) {
    console.error('Error:', error);
  }
});