const authUrl = "https://jumbogps-auth-sse.anugrahsinghal.repl.co/authenticate";
let username = document.getElementsByName("username")[0];
let password = document.getElementsByName("password")[0];
const form = document.getElementsByTagName("form")[0];

form.onsubmit = handleLogin;

function handleLogin(e) {
	e.preventDefault();

	let username = document.getElementsByName("username")[0].value;
	let password = document.getElementsByName("password")[0].value;

	axios({
		method: 'post',
		url: authUrl,
		data: {username, password}
	}).then(({data}) => {
		localStorage.setItem("token", data.token);
		window.location.href = "cluster.html";
	})
		.catch(() => {
			handleAuthFailure();
		});
}

function handleAuthFailure() {
	const errors = document.getElementsByClassName('error');

	let username = document.getElementsByName("username")[0];
	let password = document.getElementsByName("password")[0];

	for (let x = 0; x < errors.length; x++) {
		errors[x].style.opacity = 1;
		errors[x].innerHTML = 'Invalid username/password.Please try again';
	}

	username.addEventListener('focus', () => {
		for (let x = 0; x < errors.length; x++) {
			errors[x].style.opacity = 0;
		}
	})
	password.addEventListener('focus', () => {
		for (let x = 0; x < errors.length; x++) {
			errors[x].style.opacity = 0;
		}
	})
}

