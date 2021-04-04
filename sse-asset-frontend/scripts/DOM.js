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
handleInputLabelTransition();