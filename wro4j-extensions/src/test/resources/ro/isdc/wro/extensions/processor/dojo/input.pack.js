(function($){
$.fn.hoverIntent=function(f,g){
var _1={sensitivity:7,interval:100,timeout:0};
_1=$.extend(_1,g?{over:f,out:g}:f);
var cX,cY,pX,pY;
var _2=function(ev){
cX=ev.pageX;
cY=ev.pageY;
};
var _3=function(ev,ob){
ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);
if((Math.abs(pX-cX)+Math.abs(pY-cY))<_1.sensitivity){
$(ob).unbind("mousemove",_2);
ob.hoverIntent_s=1;
return _1.over.apply(ob,[ev]);
}else{
pX=cX;
pY=cY;
ob.hoverIntent_t=setTimeout(function(){
_3(ev,ob);
},_1.interval);
}
};
var _4=function(ev,ob){
ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);
ob.hoverIntent_s=0;
return _1.out.apply(ob,[ev]);
};
var _5=function(e){
var p=(e.type=="mouseover"?e.fromElement:e.toElement)||e.relatedTarget;
while(p&&p!=this){
try{
p=p.parentNode;
}
catch(e){
p=this;
}
}
if(p==this){
return false;
}
var ev=jQuery.extend({},e);
var ob=this;
if(ob.hoverIntent_t){
ob.hoverIntent_t=clearTimeout(ob.hoverIntent_t);
}
if(e.type=="mouseover"){
pX=ev.pageX;
pY=ev.pageY;
$(ob).bind("mousemove",_2);
if(ob.hoverIntent_s!=1){
ob.hoverIntent_t=setTimeout(function(){
_3(ev,ob);
},_1.interval);
}
}else{
$(ob).unbind("mousemove",_2);
if(ob.hoverIntent_s==1){
ob.hoverIntent_t=setTimeout(function(){
_4(ev,ob);
},_1.timeout);
}
}
};
return this.mouseover(_5).mouseout(_5);
};
})(jQuery);