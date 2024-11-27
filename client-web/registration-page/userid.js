// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
  // Extract the userId from the URL
  const params = new URLSearchParams(window.location.search);
  const userId = params.get('userId');

  // Display the userId on the page
  document.getElementById('userIdDisplay').textContent = userId;

  // Add click event to the "Go to Homepage" button
  document.getElementById('home-button').addEventListener('click', function() {
    window.location.replace('../home-page/homepage.html');
  });
});