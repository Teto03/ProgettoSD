document.addEventListener('DOMContentLoaded', function() {
  const domainDetails = JSON.parse(sessionStorage.getItem('domainDetails'));

  if (domainDetails) {
    const domainElement = document.querySelector('.domain');
    domainElement.textContent = domainDetails.domainId;

    const priceElement = document.querySelector('.price');
    priceElement.textContent = `${sessionStorage.getItem(domainDetails.domainId)}$/month`;
  }

  document.getElementById('domain-renew-form').addEventListener('submit', async function(event) {
    event.preventDefault();

    const formData = new FormData(this);

    const yearsToAdd = parseInt(formData.get('duration'));
    const previousDuration = parseInt(domainDetails.duration);

    const jsonData = {};
    const jsonOrderData = {"domainId": "","userId": "","orderData": "","type": "","price": ""};

    // Update domainDetails with new form data
    formData.forEach((value, key) => {
      jsonData[key] = value;
    });


    // Validate the card number length
    const cardNumber = formData.get('cardNumber');
    if (!/^\d{16}$/.test(cardNumber)) {
      alert('The card number must be exactly 16 digits long.');
      return;
    }

    // Validate the expiration date format (MM-YY)
    const cardExpirationDate = formData.get('cardExpirationDate');
    const expirationDateRegex = /^(0[1-9]|1[0-2])\/\d{2}$/;
    if (!expirationDateRegex.test(cardExpirationDate)) {
      alert('Invalid expiration date format. Please use MM/YY format.');
      return;
    }

    // Parse the expiration date and create a Date object set to the last day of the expiration month
    const [month, year] = cardExpirationDate.split('/');
    const expiration = new Date(`20${year}`, month, 0); // Month is 1-indexed by JavaScript Date, day 0 goes to last day of previous month

    // Get the current date and adjust to the first day of the current month for comparison
    const currentDate = new Date();
    currentDate.setDate(1);
    currentDate.setHours(0, 0, 0, 0); // Remove time part

    // Check if the expiration date is before the current date
    if (expiration < currentDate) {
      alert('The expiration date is before the current date. Please enter a valid expiration date.');
      return;
    }

    // Validate the CVV length
    const cvv = formData.get('CVV');
    if (!/^\d{3}$/.test(cvv)) {
      alert('The CVV must be exactly 3 digits long.');
      return;
    }

    // Validate the duration input and ensure total duration does not exceed 10 years
    if (isNaN(yearsToAdd) || yearsToAdd < 1 || yearsToAdd > 10) {
      alert('Please make sure that the duration to add is between 1 and 10 years.');
      return;
    }

    const newDuration = previousDuration + yearsToAdd;

    if (newDuration > 10) {
      alert('The total duration after renewal cannot exceed 10 years. Please adjust the duration.');
      return;
    }

    // Update domainDetails with the new duration
    domainDetails.duration = newDuration;

    jsonOrderData.domainId = domainDetails.domainId;
    jsonOrderData.userId = domainDetails.userId;
    jsonOrderData.orderDate = new Date().toISOString().slice(0, 10);
    jsonOrderData.price = sessionStorage.getItem(domainDetails.domainId);
    jsonOrderData.type = 'renewal';

    // try to log the order in the database collection named orders
    try {
      const orderResponse = await fetch('http://localhost:8080/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(jsonOrderData)
      });

      if (orderResponse.status !== 200) {
        throw new Error('Failed to log order. Server responded with status code: ' + orderResponse.status);
      }
    } catch (error) {
      console.error('Error logging order:', error.message);
      alert('Error logging order. Please try again.');
      return; // Stop further execution in case of error
    }

    try {
      const response = await fetch(`http://localhost:8080/domains`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(domainDetails)
      });

      const responseJson = await response.json();

      if (response.status === 200) {
        alert('Domain renewed successfully until ' + responseJson.expirationDate + '!');
        resetRenewForm();
        window.location.replace('../home-page/homepage.html');
      } else {
        alert('Failed to renew domain. Server responded with status code: ' + response.status);
      }
    } catch (error) {
      console.error('Error:', error.message);
      alert('Error renewing domain. Please try again.');
    }
  });
});

function resetRenewForm() {
  document.getElementById('domain-renew-form').reset();
}