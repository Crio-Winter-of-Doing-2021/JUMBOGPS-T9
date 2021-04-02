if(localStorage.getItem('token') === undefined || localStorage.getItem('token') === null) {
    console.log("not signed in so redirecting");
    window.location.assign("login.html");
}