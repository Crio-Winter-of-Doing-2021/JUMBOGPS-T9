function handleInputLabelTransition(){
    let text = document.getElementsByClassName("effect-17");
		
    for(let i=0;i<text.length;i++){
    text[i].addEventListener("blur",function(){
    if(text[i].value != ""){
    text[i].classList.add("has-content");
          }else{
            text[i].classList.remove("has-content");
          }
        })
      }
}
function showPopupNotification(msg="Logged In"){

  console.log(localStorage.getItem("initial"));

  if(msg=="Logged In" && localStorage.getItem("initial")=="false"){
    console.log('did it');
    return;
  }

  localStorage.setItem("initial","false");

	let notification = document.getElementsByClassName('notification')[0];
  
  if(msg=="Logged In" || msg=="Logged Out")
	notification.innerHTML = `
  <i class="far fa-check-circle"></i>${msg}
  `;

  else notification.innerHTML = `<i class="fas fa-exclamation-circle"></i>${msg}`;

    notification.className = "notification notification-show";

    setTimeout(()=>{
      notification.className = "notification";
    },4000)
}
handleInputLabelTransition();


let clearInputs = document.querySelectorAll(".fa-times");

for(let i=0;i<clearInputs.length;i++){
  clearInputs[i].addEventListener('click',()=>{
    clearInputs[i].previousElementSibling.value="";
    clearInputs[i].previousElementSibling.classList.remove("has-content");
  })
}

function toggleDrawer(){
  let drawer = document.getElementsByClassName("drawer")[0];
  let map = document.getElementById('map');

    drawer.style.animation = 'slideUp 4s all';
    map.style.animation = 'slideDown 4s all';
    

}

let formToggler = document.getElementsByClassName('fas fa-chevron-up')[0];

// formToggler.addEventListener('click',toggleDrawer);
