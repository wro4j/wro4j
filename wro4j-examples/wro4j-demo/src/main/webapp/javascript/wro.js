;(function(){
	if(window.WRO!=undefined){
		return;
	}
	var wro=function(){
	    this.callbacks=[];
	};
	wro.prototype.onReady=function(callback){
	    callback();
	};
	wro.prototype.fireReady=function(){
	  
	}; 
	window.WRO=new wro();
	
})();