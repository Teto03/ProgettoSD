document.addEventListener('DOMContentLoaded', function() {
  const domainId = sessionStorage.getItem('domainToBuy');
  const domainToRelease = sessionStorage.setItem('domainToRelease', domainId);

  if (domainId) {
    const domainElement = document.querySelector('.domain');
    domainElement.textContent = domainId;

    const priceElement = document.querySelector('.price');
    const price = generateDomainPrice(domainId);
    priceElement.textContent = `${price}$/month`;
  }

  const timeoutId = setTimeout(() => {
    alert('You have been on this page for more than 1 minute. Redirecting to the homepage.');
    window.location.replace('../home-page/homepage.html');
  }, 60000); // 1 minute in milliseconds

  document.getElementById('domain-purchase-form').addEventListener('submit', async function(event) {
    event.preventDefault();

    const formData = new FormData(this);
    const jsonData = {};
    const jsonOrderData = {"domainId": "","userId": "","orderData": "","type": "","price": ""};

    formData.forEach((value, key) => {
      jsonData[key] = value;
    });

    // Validate the card number length
    const cardNumber = formData.get('cardNumber');
    if (!/^\d{16}$/.test(cardNumber)) {
      alert('The card number must be exactly 16 digits long.');
      return;
    }

    // Validate the expiration date format (MM/YY)
    const cardExpirationDate = formData.get('cardExpirationDate');
    const expirationDateRegex = /^(0[1-9]|1[0-2])\/\d{2}$/;
    if (!expirationDateRegex.test(cardExpirationDate)) {
      alert('Invalid expiration date format. Please use MM/YY format.');
      return;
    }

    // Parse the expiration date and create a Date object set to the last day of the expiration month
    const [month, year] = cardExpirationDate.split('/');
    const expiration = new Date(`20${year}`, month, 0);

    // Get the current date and adjust to the first day of the current month for comparison
    const currentDate = new Date();
    currentDate.setDate(1);
    currentDate.setHours(0, 0, 0, 0);

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

    // Validate the duration
    const durationYears = parseInt(formData.get('duration'), 10);
    if (isNaN(durationYears) || durationYears < 1 || durationYears > 10) {
      alert('Please enter a duration between 1 and 10 years.');
      return;
    }

    // Calculate the domain expiration date
    const today = new Date();
    const expirationYear = today.getFullYear() + durationYears;
    const domainExpirationDate = new Date(today.setFullYear(expirationYear)).toISOString().split('T')[0];


    if (domainId) {
      jsonData.domainId = domainId;
      jsonOrderData.domainId = domainId;
    }

    const userId = sessionStorage.getItem('sessionToken');

    jsonData.userId = userId;
    jsonOrderData.userId = userId;
    jsonData.currentDate = today.toISOString().slice(0, 10);
    jsonOrderData.orderDate = new Date().toISOString().slice(0, 10);

    jsonOrderData.type = 'buy';
    jsonOrderData.price = sessionStorage.getItem(domainId);

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
      const response = await fetch('http://localhost:8080/domains', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(jsonData)
      });

      if (response.status === 200) {
        alert('Domain buyed successfully until ' + domainExpirationDate + '!');
        resetBuyForm();
        window.location.replace('../home-page/homepage.html');
      } else {
        alert('Failed to purchase domain. Server responded with status code: ' + response.status);
      }
    } catch (error) {
      console.error('Error:', error.message);
      alert('Error purchasing domain. Please try again.');
    }

    clearTimeout(timeoutId); // Clear the timeout if the form is submitted
  });
});

function generateDomainPrice(domainId) {
  let price = sessionStorage.getItem(domainId);

  if (!price) {
    price = Math.floor(Math.random() * (40 - 20 + 1)) + 20;
    sessionStorage.setItem(domainId, price.toString());
  }

  return price;
}

function resetBuyForm() {
  document.getElementById('domain-purchase-form').reset();
}
