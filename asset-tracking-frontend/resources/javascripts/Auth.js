const authUrl = "https://jumbogps-auth-sse.anugrahsinghal.repl.co/authenticate";
const indexPageURL = "https://127.0.0.1:5500/asset-tracking-frontend/resources/templates/index.html";

const form = document.getElementsByTagName("form")[0];

form.onsubmit =  handleLogin;

 function handleLogin(e){
    e.preventDefault();
    let username = document.getElementsByName("username")[0].value;
    let password = document.getElementsByName("password")[0].value;

    axios({
        method: 'post',
        url: authUrl,
        data: {username,password}
      }).then(({data})=>{
          localStorage.setItem("token",data.token);
          window.location.href = indexPageURL;
        })
      .catch(()=> console.log('login failed!!'));
}

