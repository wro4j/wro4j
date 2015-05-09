//category-------------------------------------------begin
(function () {
	var categoryList=null;
	//类目列表，包括优化符合的类目，优化但不符合的类目，及控制用的更多或精-简
	var qualifiedList=null;
	//优化合格的类目
	var unqualifiedList=null;
	//优化不合格的类目
	var g_qualifiedList=new Array();
	var g_unqualifiedList=new Array();
	var oCtrl=new Array();
	ListCategory={
		/**
		* 根据类目DOM的ID激活类目层
		* @param {Object} categoryListId 类目DOM的ID
		*/
		active:function (categoryListId) {
			if(oCtrl[categoryListId]) {
				this.showMoreOrLess(categoryListId);
			}else {
				this.init(categoryListId);
				this.showMoreOrLess(categoryListId);
			}
		},
		/**
		* 根据类目DOM的ID初始化类目
		* @param {Object} categoryListId 类目DOM的ID
		*/
		init:function (categoryListId)
		{
			var oCategoryList=document.getElementById(categoryListId);
			if(oCategoryList)
			{
				qualifiedList=new Array();
				g_qualifiedList[categoryListId]=qualifiedList;
				unqualifiedList=new Array();
				g_unqualifiedList[categoryListId]=unqualifiedList;
				categoryList=oCategoryList.getElementsByTagName("li");
				for(var i=0;i<categoryList.length;i++)
				{
					if(categoryList[i].className=="qualified")
					{
						qualifiedList[qualifiedList.length]=categoryList[i];
					}
					else if(categoryList[i].className=="unqualified")
					{
						unqualifiedList[unqualifiedList.length]=categoryList[i];
					}
					else if(categoryList[i].className=="category_ctrl")
					{
						oCtrl[categoryListId]={
							aLink:categoryList[i].getElementsByTagName("a")[0],stat:'less',moreText:'显示更多&gt;&gt;',lessText:'&lt;&lt;精简显示'
						};
					}
				}
			}
		},
		/**
		* 类目精-简方式显示或全部显示
		*/
		showMoreOrLess:function (categoryListId) {
			if(oCtrl[categoryListId].stat=="less") {
				this.showMore(categoryListId);
			}else {
				this.showLess(categoryListId);
			}
		},
		/**
		* 全部显示类目,并更改当前状态。"more"为全部显示状态，"less"为精-简显示状态。
		*/
		showMore:function (categoryListId) {
			oCtrl[categoryListId].stat="more";
			var unqualifiedList=g_unqualifiedList[categoryListId];
			for(var i=0;i<unqualifiedList.length;i++) {
				unqualifiedList[i].style.display="block";
			}
			oCtrl[categoryListId].aLink.innerHTML=oCtrl[categoryListId].lessText;
		},
		/**
		* 精-简方式显示类目
		*/
		showLess:function (categoryListId) {
			oCtrl[categoryListId].stat="less";
			var unqualifiedList=g_unqualifiedList[categoryListId];
			for(var i=0;i<unqualifiedList.length;i++) {
				unqualifiedList[i].style.display="none";
			}
			oCtrl[categoryListId].aLink.innerHTML=oCtrl[categoryListId].moreText;
		}
	}
})();
function showDefCategory() {
	document.getElementById("list_cs_title").innerHTML="<strong>按类目选择</storng>";
	document.getElementById("list_cs_content").style.display="block";
}
function showDefCategoryAdv(keyWord,isOpen) {
	if(keyWord) {
		var list_cs_title=document.getElementById("list_cs_title");
		var list_cs_content=document.getElementById("list_cs_content");
		if(isOpen) {
			list_cs_title.innerHTML="<strong><a href=\"javascript:showDefCategoryAdv('"+keyWord+"',false)\">缩略\"<span class=\"orange\">"+keyWord+"</span>\"所有类目</a></strong>"
			list_cs_content.style.display="block";
		}else {
			list_cs_title.innerHTML="<strong><a href=\"javascript:showDefCategoryAdv('"+keyWord+"',true)\">查看\"<span class=\"orange\">"+keyWord+"</span>\"所有类目</a></strong>";
			list_cs_content.style.display="none";
		}
	}
}
childCatgoryManage={
	/*childCategoryId:二级类目的ID
	*liclsname:动作 值有 showMore | showLess
	*curentID :当前链接的ID
	*/
	showCategory:function (childCategoryId,liclsname,curentID) {
		var showmore=document.getElementById(curentID);
		var cName=liclsname=="showMore"?"showLess":"showMore";
		if(showmore)
		{
			if(showmore.className=="c_show")
			{
				showmore.innerHTML="<a  href=javascript:childCatgoryManage.showCategory('"+childCategoryId+"','"+cName+"','"+curentID+"')>&lt;&lt;精简显示</a>";
				showmore.className="c_hidden";
			}
			else
			{
				showmore.innerHTML="<a  href=javascript:childCatgoryManage.showCategory('"+childCategoryId+"','"+cName+"','"+curentID+"')>显示更多&gt;&gt;</a>";
				showmore.className="c_show";
			}
		}
		var e=document.getElementById(childCategoryId);
		if(e) {
			for(var j=0;j<e.childNodes.length;j++) {
				var child=e.childNodes.item(j);
				if(child&&(child.tagName=='li'||child.tagName=='LI')&&child.className&&child.className==cName) {
					child.className=liclsname;
				}
			}
		}
	}
}
//category-------------------------------------------------------------------------------------------------------end

function doQuickFilt() {
	quickfilt();
}



//checkbox --------------------------------------------------------------------------------------------------begin
/**
* @method actCheckbox
* @param {String} sign
* @param {String} keywords
* @static
*/
function actCheckbox(sign,keywords) {
	var count=0;
	var ids="";
	var obj=document.batchForm.elements;
	for(var i=0;i<obj.length;i++) {
		if(obj[i].id.indexOf('compareBox')!=-1) {
			if(obj[i].checked) {
				count++;
				if(ids=="") {
					ids=obj[i].name;
				}else {
					ids=ids+","+obj[i].name;
				}
			}
		}
	}
	if(count<1) {
		alert("请至少选择1条信息进行询价!");
		return false;
	}else if(count>5) {
		alert("您已经选择了"+count+"条供应信息,候选供应信息不能超过5条");
		return false;
	}
	var url="";
	if(sign=="inc") {
		url="http://redirect.china.alibaba.com/gateway/10023"+"?offer_ids="+ids+"&keywords="+keywords+"&tracelog=saleofferlist_feedbacks";
	}else if(sign=="noimg") {
		url="http://redirect.china.alibaba.com/gateway/10023"+"?offer_ids="+ids+"&keywords="+keywords+"&tracelog=saleofferlist_feedbacks_nopic";
	}
	window.open(url);
}

/**
* 获取cookie
* @param {String} name
* @static
*/
function getCookie(name) {
	var arg=name+"=";
	var alen=arg.length;
	var clen=document.cookie.length;
	var i=0;
	while(i<clen) {
		var j=i+alen;
		if(document.cookie.substring(i,j)==arg) {
			return www_helpor_net(j);
		}
		i=document.cookie.indexOf(" ",i)+1;
		if(i==0)
		break;
	}
	return null;
}
var cookieName="offerId";
var domainName=".alibaba.com";
var prefix="";
var defaultClassName="offer";
var checkedClassName="offer_select";
var categoryId=0;
var inside_cooperate_id=false;
var tempCount=0;
var url;
//初始化cookie中的offerId
function initOfferId() {
	var c=getCookie("categoryId");
	if(c==null||c=="null") {
		addCookie("categoryId",categoryId);
		if(categoryId!=null&&categoryId!="") {
			clearCookie(cookieName);
		}
	}else if(c!=categoryId) {
		clearCookie("categoryId");
		clearCookie(cookieName);
	}
}
function addCookie(name,value) {
	value=value+prefix;
	if(categoryId=="")return ;
	document.cookie=name+"="+value+";path=/;"+"domain="+domainName;
	;
}
function clearCookie(name) {
	var value="null";
	cookieValue=name+"="+value+";path=/;"+"domain="+domainName;
	document.cookie=cookieValue;
}
function addOfferId(arg,box) {
	arg=arg+prefix;
	if(hasOfferId(arg)) {
		//alert("要选择的商机已存在");
		box.checked=false;
		return ;
	}
	if(arg=="") {
		return ;
	}
	value=getCookie(cookieName);
	if(value==null||value=="null") {
		value=arg;
	}else {
		value=value+"x"+arg;
	}
	cookieValue=cookieName+"="+value+";path=/;"+"domain="+domainName;
	document.cookie=cookieValue;
	var countId=box.id.substring("compareBox".length,box.id.length);
	var trTag=document.getElementById("compareColor"+countId);
	trTag.className=checkedClassName;
}
function delOfferId(arg) {
	arg=arg+prefix;
	if(!hasOfferId(arg)) {
		return ;
	}
	value=getCookie(cookieName);
	var offerIds=stringToArray(value);
	var len=offerIds.length;
	var result="";
	var counter=0;
	for(var i=0;i<len;i++) {
		if(offerIds[i]!=arg) {
			if(counter==0) {
				result=offerIds[i];
			}else {
				result=result+"x"+offerIds[i];
			}
			counter++;
		}
	}
	cookieValue=cookieName+"="+result+";path=/;"+"domain="+domainName;
	document.cookie=cookieValue;
}
function hasOfferId(arg) {
	var value=getCookie(cookieName);
	if(value==null||value=="null"||value.indexOf(arg)==-1) {
		return false;
	}else {
		return true;
	}
}
function getOfferIdCount() {
	value=getCookie(cookieName);
	if(value==null||value=='null') {
		return 0;
	}
	var count=1;
	while(value.indexOf("x")!=-1) {
		var position=value.indexOf("x");
		var len=value.length;
		value=value.substring(position+1,len);
		count++;
	}
	return count;
}
function stringToArray(str) {
	var temp=str;
	var strArray=new Array();
	var count=0;
	while(temp.indexOf("x")!=-1) {
		var position=temp.indexOf("x");
		strArray[count]=temp.substring(0,position);
		count++;
		var len=temp.length;
		temp=temp.substring(position+1,len);
	}
	strArray[count]=temp;
	return strArray;
}
function clickcompareBox(id,box) {
	if(box.checked) {
		if(getOfferIdCount()>=10&&!inside_cooperate_id) {
			alert('您已经选择了10条供应信息，候选供应信息不能超过10个');
			box.checked=false;
		}else {
			addOfferId(id,box);
		}
	}else {
		delOfferId(id);
		var countId=box.id.substring("compareBox".length,box.id.length);
		var trTag=document.getElementById("compareColor"+countId);
		trTag.className=defaultClassName;
	}
}
function doCompare() {
	if(getOfferIdCount()<=1) {
		alert('请至少选择2条信息进行对比!');
	}else if(getOfferIdCount()>10) {
		alert('您已经选择了10条供应信息，候选供应信息不能超过10个');
	}else {
		window.open(url+'/offer/offer_compare.htm');
	}
}
function cleanCookie() {
	if(confirm("您确认执行清空操作吗？")) {
		var value="null";
		cookieValue=cookieName+"="+value+";path=/;"+"domain="+domainName;
		document.cookie=cookieValue;
		for(var i=1;i<=tempCount;i++) {
			var elem=document.getElementById("compareBox"+i);
			if(elem!=null&&elem.checked) {
				elem.checked=false;
				elem=document.getElementById("compareColor"+i);
				elem.className=defaultClassName;
			}
		}
	}
}
function iniOfferId(arg,value) {
	if(value==null||value=="null"||value.indexOf(arg)==-1) {
		return false;
	}else {
		return true;
	}
}
//初始化
function initCheckbox(offerCount,catId,insideCooperateId,currentServer) {
	tempCount=offerCount+1;
	categoryId=catId;
	inside_cooperate_id=insideCooperateId;
	url=currentServer;
	initOfferId();
	var tempValue=getCookie(cookieName);
	for(var i=1;i<tempCount;i++) {
		var cpbox=document.getElementById("compareBox"+i);
		var cpcolor=document.getElementById("compareColor"+i);
		if(cpbox!=null) {
			if(iniOfferId(cpbox.name,tempValue)) {
				cpbox.checked=true;
				if(cpcolor!=null) {
					cpcolor.className=checkedClassName;
				}
				tempValue=tempValue.replace(cpbox.name,"");
			}else {
				cpbox.checked=false;
			}
		}
	}
}

function boxSearchCheck(obj,searchTrace) {
	var k=obj.keywords;
	if(k) {
		var v=k.value;
		if(v.length>100) {
			alert("您输入的关键字过长！");
			k.select();
			return false;
		}
		if(v==""||v.substring(0,3)=="请输入") {
			alert("请输入关键字！");
			k.focus();
			return false;
		}
	}
	var tracelog="?searchtrace="+searchTrace;
	aliclick(this,tracelog);
}
function topSearchCheck(obj,searchTopTrace) {
	return boxSearchCheck(obj,searchTopTrace);
}
function bottomSearchCheck(obj,searchBottomTrace) {
	return boxSearchCheck(obj,searchBottomTrace);
}
function zeroSearchCheck(obj,searchZeroTrace) {
	return boxSearchCheck(obj,searchZeroTrace);
}
function shieldOncontextmenu() {
	var target=getEvent().target||getEvent().srcElement;
	// 获得事件源
	if(target.src) {
		return false;
	}
}
function getEvent(e) {
	var ev=e||window.event;
	if(!ev) {
		var c=this.getEvent.caller;
		while(c) {
			ev=c.arguments[0];
			if(ev&&Event==ev.constructor ) {
				break;
			}
			c=c.caller;
		}
	}
	return ev;
}
//禁止右键点击图片链接
function unpremitImgUrl() {
	document.oncontextmenu=shieldOncontextmenu
}

function popwin(theurl)
{
	window.open(theurl,'','height=400, width=500, top=60, left=150, toolbar=no, menubar=no, scrollbars=yes, resizable=no,location=no, status=no');
}
(function () {
	var rkCookieName="h_keys";
	//记录最近搜索词,零结果页面不记录
	window.recordKeywords=function (keywords,isZeroResult) {
		if(keywords!=""&&keywords.length<10&&!isZeroResult) {
			var needWrite=true;
			var key=keywords;
			var h_keys_v=key;
			var keys=getCookie(rkCookieName);
			if(keys!=null) {
				var keys_array=keys.split("#");
				for(var i=0;i<keys_array.length&&i<9;i++) {
					if(key==keys_array[i]) {
						if(i==0) {
							needWrite=false
						}
					}else {
						h_keys_v+="#"+keys_array[i];
					}
				}
			}
			if(needWrite) {
				addTimeCookie(rkCookieName,h_keys_v);
			}
		}
	}
})();
function addTimeCookie(name,value) {
	var expDate=new Date()
	expDate.setYear(expDate.getYear()+1);
	document.cookie=name+"="+escape(value)+";expires="+expDate.toGMTString()+";path=/;"+"domain="+domainName;
}
(function () {
	var _trustscoreUrl;
	var _activeScoreUrl;
	var _defaultSortUrl;
	var _pageUrl;
	var _showfeatureUrl;
	var _noimgStyleUrl;
	var _imgStyleUrl;
	var _shopwindowStyleUrl;
	//初始化url
	window.initSortTypeUrl=function (trustscoreUrl,activeScoreUrl,defaultSortUrl,pageUrl,showfeatureUrl,noimgStyleUrl,imgStyleUrl,shopwindowStyleUrl) {
		_trustscoreUrl=trustscoreUrl;
		_activeScoreUrl=activeScoreUrl;
		_defaultSortUrl=defaultSortUrl;
		_pageUrl=pageUrl;
		_showfeatureUrl=showfeatureUrl;
		_noimgStyleUrl=noimgStyleUrl;
		_imgStyleUrl=imgStyleUrl;
		_shopwindowStyleUrl=shopwindowStyleUrl;
	}

	//默认排序
	window.sort_by_default=function () {
		if(_defaultSortUrl!="") {
			window.location.href=_defaultSortUrl;
		}
	}
	/**
	* 属性下拉框
	* @method change_show_fid
	* @param {Int} v
	* @static
	*/
	window.change_show_fid=function (v) {
		if(_pageUrl.indexOf("?")>0) {
			window.location.href=_showfeatureUrl+"&showFid="+v;
		}else {
			window.location.href=_showfeatureUrl+"?showFid="+v;
		}
	}
	/**
	* 展示方式切换
	* @method showstyle
	* @param {String} v
	* @static
	*/
	window.showstyle=function (v) {
		if(v=="noimg") {
			window.location.href=_noimgStyleUrl;
		}else if(v=="img") {
			window.location.href=_imgStyleUrl;
		}else if(v=="shopwindow") {
			window.location.href=_shopwindowStyleUrl;
		}
	}
})();
/**
* 地区下拉框回显
* @method changeprovince
* @param {String} province
* @param {String} beforeProvince
* @param {String} beforeCity
* @static
*/
function changeprovince(province,beforeProvince,beforeCity) {
	if(province!=null) {
		for(var i=0;i<province.options.length;i++) {
			var provinceValue=province.options[i].value;
			var cityValue=province.options[i].getAttribute("city");
			if(provinceValue==beforeProvince&&(!cityValue||cityValue==beforeCity)) {
				province.options[i].selected=true;
				break;
			}
		}
	}
}
/**
* 地区下拉框筛选
* @method filtByCentralArea
* @param {Object} optionObj
* @static
*/
function filtByCentralArea(optionObj) {
	var optionElem=optionObj.options[optionObj.selectedIndex];
	var province=optionElem.value;
	var city=optionElem.getAttribute("city");
	changeRegion(province,city);
	try{
		document.getElementById("sx_ctrl_btn").click();
	}catch(e) {
	}
}
 
function focusit(inputobj) {
	if(inputobj.value.indexOf('请输入')!=-1)inputobj.value='';
	inputobj.style.color="#666666";
}
function showLessOrMore(obj) {
	if(obj.className=="ml5px less") {
		if(document.getElementById('sx_self_param')) {
			document.getElementById('sx_more_param').style.display="none";
			document.getElementById('sx_top').style.height="36px";
			document.getElementById('sx_ctrl_btn').className="sx_but_small";
		}else {
			document.getElementById('sx_top').style.display="none";
		}
		obj.className="ml5px more";
		obj.innerHTML="<span class='ml15px'>更多</span>";
		document.frmAreaSearch.sm.value="";
	}else {
		if(document.getElementById('sx_self_param')) {
			document.getElementById('sx_more_param').style.display="block";
			document.getElementById('sx_top').style.height="62px";
			document.getElementById('sx_ctrl_btn').className="sx_but_big";
		}else {
			document.getElementById('sx_top').style.display="block";
		}
		obj.className="ml5px less";
		obj.innerHTML="<span class='ml15px'>精简</span>";
		document.frmAreaSearch.sm.value="true";
	}
}
function displayMore() {
	var obj=document.getElementById("sx_ctrl_a");
	obj.className="ml5px more";
	showLessOrMore(obj);
}
function doQuickFilt() {
	quickfilt();
}
function setOnlineStatus() {
	var onlineStatus=document.getElementById("_onlineStatus").value;
	if(onlineStatus=="all"||onlineStatus=="") {
		onlineStatus="yes";
	}else if(onlineStatus=="yes") {
		onlineStatus="all";
	}
	document.getElementById("_onlineStatus").value=onlineStatus;
}

var qFilt="_";
var oFilt="_";
var count;
var trace_f="";
var provincesel;
var citysel;
var _province;
var abroadTraceLog;
var escapeKeywords;
var traceHead;

function initFIltParam(escapeKeywords,abroadTraceLog,traceHead) {
	this.provincesel=document.getElementById("province_slt");
	this.citysel=document.getElementById("city_slt");
	this._province=document.getElementById("_province");
	this.abroadTraceLog=abroadTraceLog;
	this.escapeKeywords=escapeKeywords;
	this.traceHead=traceHead;
} 
/**
* checkBox按钮组对象
*/
function CheckBox(divHeight) {
	this.divHeight=divHeight;
	this.clickCount=0;
	this.checkCount=0;
	//当前选中的CHECK
	this.checkBoxChilds=new Array();
}
CheckBox.prototype={
	init:function (checkEl) {
		for(var i=0;i<this.checkBoxChilds.length;i++) {
			if(this.checkBoxChilds[i].checked==true) {
				this.checkCount++;
			}
		}
		if(checkEl.checked==true) {
			this.checkCount=this.checkCount-1;
		}
		if(checkEl.checked==false) {
			this.checkCount=this.checkCount+1;
		}
	},
	/**
	* 获取当前check在checkBox中的序列
	*/
	getNum:function (checkEl) {
		for(var i=0;i<this.checkBoxChilds.length;i++) {
			if(this.checkBoxChilds[i].id==checkEl.id) {
				return i;
			}
		}
	},
	/**
	* 根据在checkBox中的序列返回相应的check对象
	*/
	getCheck:function (num) {
		return this.checkBoxChilds[num];
	},
	/**
	* 加载checkbox对象到checkbox组中
	*/
	add:function (checkEl) {
		this.checkBoxChilds[this.checkBoxChilds.length]=document.getElementById(checkEl);
	},
	/**
	* 获取离当前checkbox最近的选中的checkbox的序列
	*/
	getNearCheckedNum:function (checkEl) {
		var checkElNum=this.getNum(checkEl);
		var upCheckedNum=this.getUpCheckedNum(checkElNum);
		var downCheckedNum=this.getDownCheckedNum(checkElNum);
		if(upCheckedNum!=null&&downCheckedNum!=null) {
			if((checkElNum-upCheckedNum)<=(downCheckedNum-checkElNum)) {
				return upCheckedNum;
			}else {
				return downCheckedNum;
			}
		}else if(upCheckedNum==null&&downCheckedNum!=null) {
			return downCheckedNum;
		}else if(upCheckedNum!=null&&downCheckedNum==null) {
			return upCheckedNum;
		}else {
			return checkElNum;
		}
	},
	/**
	* 向上获取最近的选中的checkbox的序列
	*/
	getUpCheckedNum:function (num) {
		for(var i=num-1;i>=0;i--) {
			if(this.checkBoxChilds[i].checked) {
				return i;
			}
		}
		return null;
	},
	/**
	* 向下获取最近的选中的checkbox的序列
	*/
	getDownCheckedNum:function (num) {
		for(var i=num+1;i<this.checkBoxChilds.length;i++) {
			if(this.checkBoxChilds[i].checked) {
				return i;
			}
		}
		return null;
	},
	changecheckStat:function (checkEl) {
		if(this.clickCount==0) {
			this.init(checkEl);
		}
		this.clickCount++;
		if(checkEl.checked) {
			this.checkCount++;
			if(this.checkCount>=2) {
				this.showFloatDiv(checkEl);
			}else {
				this.hiddenFlaotDiv();
			}
		}else {
			this.checkCount--;
			if(this.checkCount>=2) {
				this.showFloatDiv(this.getCheck(this.getNearCheckedNum(checkEl)));
			}else {
				this.hiddenFlaotDiv();
			}
		}
	},
	clearCount:function () {
		this.clickCount=0;
		this.checkCount=0;
		this.hiddenFlaotDiv();
	},
	/**
	* 显示并定位提示浮动层
	*/
	showFloatDiv:function (checkEl) {
		document.getElementById("tishiDiv").style.display="block";
		document.getElementById("tishiDiv").style.position="absolute";
		document.getElementById("tishiDiv").style.top=getXY(checkEl)[1]-this.divHeight+"px";
		document.getElementById("tishiDiv").style.left=getXY(checkEl)[0]-10+"px";
	},
	/**
	* 隐藏提示浮动层
	*/
	hiddenFlaotDiv:function () {
		document.getElementById("tishiDiv").style.display="none";
	}
}
/**
* 获取对象el的X,Y坐标
* @param {Object} el
*/
function getXY(el) {
	var pos;
	if(this.getExplorerType()==1) {
		var scrollTop=Math.max(document.documentElement.scrollTop,document.body.scrollTop);
		var scrollLeft=Math.max(document.documentElement.scrollLeft,document.body.scrollLeft);
		pos=[el.getBoundingClientRect().left+scrollLeft,el.getBoundingClientRect().top+scrollTop];
	}else {
		pos=[el.offsetLeft,el.offsetTop];
		var parentNode=el.offsetParent;
		if(parentNode!=el) {
			while(parentNode) {
				pos[0]+=parentNode.offsetLeft;
				pos[1]+=parentNode.offsetTop;
				parentNode=parentNode.offsetParent;
			}
		}
		if(el.parentNode) {
			parentNode=el.parentNode;
		}
		else {
			parentNode=null;
		}
		while(parentNode&&parentNode.tagName.toUpperCase()!='BODY'&&parentNode.tagName.toUpperCase()!='HTML') {
			if(parentNode.style.display!='inline') {
				pos[0]-=parentNode.scrollLeft;
				pos[1]-=parentNode.scrollTop;
			}
			if(parentNode.parentNode) {
				parentNode=parentNode.parentNode;
			}else {
				parentNode=null;
			}
		}
	}
	return pos;
}
function getExplorerType() {
	var ua=navigator.userAgent.toLowerCase();
	if(window.ActiveXObject) {
		return 1;
	}else if((ua.indexOf('firefox')>-1)) {
		return 2;
	}else if((ua.indexOf('opera')>-1)) {
		return 3;
	}
}
//checkbos ---------------------------------------------------------------------------end