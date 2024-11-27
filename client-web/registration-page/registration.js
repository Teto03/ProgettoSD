function resetRegistrationForm() {
  // Reset the registration form
  document.getElementById('registration-form').reset();
}

// Add event listener for form submission
document.getElementById('registration-form').addEventListener('submit', async function(event) {
  event.preventDefault(); // Prevent default form submission

  // Serialize form data into JSON
  const formData = new FormData(this);
  const jsonData = {};

  formData.forEach((value, key) => {
    jsonData[key] = value;
  });

  // First, check if the email is already in use
  try {
    const checkResponse = await fetch('http://localhost:8080/registrations', {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      }
    });

    if (!checkResponse.ok) {
      const errorDetails = await checkResponse.text();
      throw new Error('Error checking email. Server responded with: ' + errorDetails);
    }

    const emails = await checkResponse.json(); // Assuming the server returns an array of emails
    const email = formData.get('email');

    if (emails.includes(email)) {
      alert('Email is already in use. Please login or register with a different email.');
      window.location.replace('../login-page/login.html'); // Redirect to login page
      return;
    }
  } catch (error) {
    console.error('Error:', error.message);
    alert('Error checking email. Please try again.');
    return;
  }

  // Proceed with registration if email is not in use
  try {
    const response = await fetch('http://localhost:8080/registrations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(jsonData)
    });

    if (!response.ok) {
      const errorDetails = await response.text();
      throw new Error('Error registering user. Server responded with: ' + errorDetails);
    }

    // Handle successful registration
    const userId = await response.json(); // Extract user ID from response
    console.log('User ID:', userId);
    sessionStorage.setItem('sessionToken', userId); // Store the session token

    // Wait 0.5 second before redirecting
    setTimeout(() => {
      window.location.replace('./userid.html?userId=' + userId);
    }, 500);
    resetRegistrationForm(); // Reset form after successful registration

  } catch (error) {
    console.error('Error:', error.message);
    const requestBodyDebugInfo = JSON.stringify(jsonData, null, 2); // Pretty print JSON
    alert('Error registering user. Please try again. Debug info: ' + error.message + '\nRequest Body: ' + requestBodyDebugInfo);
  }
});