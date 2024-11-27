async function createCollectionOnPageLoad(collectionName) {
  try {
    // Prepara il corpo della richiesta in formato JSON con la chiave "collectionName"
    const requestBody = JSON.stringify({ collectionName });

    const response = await fetch('http://localhost:8080/collections', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: requestBody // Usa il corpo della richiesta preparato
    });

    if (!response.ok) {
      // Se il server risponde con un errore, registra i dettagli dell'errore
      const errorDetails = await response.json();
      console.error('Error creating collection. Server responded with:', errorDetails);
    } else {
      // Registra il messaggio di successo
      const successMessage = await response.json();
      console.log(successMessage);
    }
  } catch (error) {
    console.error('Error:', error);
  }
}

// Chiama la funzione createCollection non appena la pagina viene caricata
window.onload = function() {
  createCollectionOnPageLoad("registrations");
  createCollectionOnPageLoad("domains");
  createCollectionOnPageLoad("orders")
};

function resetLoginForm() {
  // Reset the login form fields
  document.getElementById('login-form').reset();
}

document.getElementById('create-account').addEventListener('click', function() {
  // Redirect to the account creation page
  window.location.replace('../registration-page/registration.html');
});

document.getElementById('login-form').addEventListener('submit', async function (event) {
  event.preventDefault(); // Prevent the form from submitting in the traditional way

  // Serialize form data into a JSON object
  const formData = new FormData(this);
  const jsonData = {};

  // Initialize userId as empty string
  jsonData.userId = "";

  formData.forEach((value, key) => {
    jsonData[key] = value;
  });

  // Attempt to log in by sending a POST request to the server
  try {
    const response = await fetch('http://localhost:8080/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(jsonData)
    });

    if (!response.ok) {
      // If the server responds with an error, throw an exception
      const errorDetails = await response.text();
      throw new Error('Error logging in. Server responded with: ' + errorDetails);
    }

    // On successful login, store the session token and redirect to the homepage
    sessionStorage.setItem('sessionToken', jsonData.userId);
    window.location.replace('../home-page/homepage.html');
    resetLoginForm(); // Reset the form fields
  } catch (error) {
    console.error('Error:', error.message);
    // Prepare debug information for the error message
    const requestBodyDebugInfo = JSON.stringify(jsonData, null, 2); // Pretty print JSON
    // Display an error alert with debug information
    alert('Error logging in. Please try again. Debug info: ' + error.message + '\nRequest Body: ' + requestBodyDebugInfo);
  }
});