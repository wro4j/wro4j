/* Copyright (c) 2005 ISDC! Romania. All rights reserved.
 *  This javascript file contains the implementation of a client-side validation object.
 *  Dependencies:none
 */
//switching debug messages on/off
var isDebug = false;
//browser detection variables
var ua = navigator.userAgent;
var isOP = /Opera/.test(ua), isMoz = /Gecko/.test(ua), isIE = !(isMoz || isOP);
var isIE5x = (navigator.appVersion.indexOf("MSIE 5.") != -1);
//automatically create the uniquely used validator instance
var validator = new Validator();
//application messages
var m = ["Element with id (0) does not exist!", "The event (0) for the element (1) does not exist!", "Invalid condition for validation:(0).(1)", "Invalid type: (0) for (1).", "There are incorrectly specified handler functions in the chain of element: (0)", "The execution order: (0) of the handler function is invalid. Valid orders are: (1).", "The expression for (0) does not evaluate to a number:(1)", "Cannot evaluate (0):(1)"];
//The Validator object. This is the main object in this file.
//Every other objects & functions are used by it.
function Validator(){
  //instance variables
  var version = this.version = "2.0.0";
  //a unique ID for the object (based on the version number)
  var uid = this.uid = "ISDC_VALIDATOR_" + version.replace(/\D/g, "");//getUID(5);
  //enforce singleton for the object
  if (typeof(window[uid]) != "undefined") {
    return this
  }
  window[uid] = this;
  _v = this;
  //end singleton part
  
  var validatedControls = [];//array of validation objects
  var controlGroups = [];//array of validation groups
  var namedGroups = [];//array of named validation groups (works as HashMap)
  var runHandleOnBind = false;//Whether to call the handlers right after validation was bound to the control.by default don't run handle on bind.
  /*generic validator function.This will be bound to the given event of the HTMLElement*/
  function _validate(e){
    if (eval(this.ctl.condition) == true) {
      this.ctl.validate();
    }
  };
  function _reset(){
    validatedControls=[];
    controlGroups=[];
    namedGroups=[];
  };  
  
  /*validated Control object
   @param id: The HTML id attribute of the HTML element to be validated. Type:String.Mandatory=yes.
   @param type: The type of the data to be validated. Can be one of the following:string,int,float,date. Mandatory=no. Default=string.
   @param event: The names of the html events triggering the validation function (eg:onblur). Mandatory=no. Default=onblur.
   @param handlers: The chain of names of the error processing functions, separated by semicolon (eg.:"showRedcross;showInPopup").
   Mandatory=no Default=showRedcross;
   */
  //{id:null,type:'string',format:'yyyy-mm-dd'events:['onblur'],handlers:[],order:'after',condition:true,msg:''}
  function validatedControl(ctl){
    var _this = this;
    this.id = ctl.id;
    this.feedbackElement=ctl.feedbackElement;
    this.msg = ctl.msg;
    this.events = ctl.events;
    this.type = ctl.type;
    this.format = ctl.format;
    this.handlers = ctl.handlers;
    this.condition = ctl.condition;
    this.element = document.getElementById(this.id);
    this.validations = [];//array of validation objects bound to the current control.
    this.allowNull = true;
    this.hasCustom = false;
    this.invalidities = null;
    this.isTypeCorrect = true;
    this.hint = null; //keeps the hint for the control if hints object is used and the control is "hinted"
    /*The validation function of the Control*/
    function typeValidate(){
      switch (_this.type) {
        case 'int':
          return isInt(_this.element.value);
        case 'float':
          return isFloat(_this.element.value);
        case 'date':
          return (_this.element.value.toDate(_this.format) != null);
      }
      return true;
    };
    this.validate = function(prmDontHandle){
      //if the condition of control is false (eg, don't care about, treat like valid)
      var ctlCond = eval(this.condition);
      if (ctlCond === false) 
        return true;
      var v = null;
      var isValid = true;
      _this.invalidities = 0;
      _this.isTypeCorrect = typeValidate();
      for (var i = 0, l = _this.validations.length; i < l; i++) {
        v = _this.validations[i];
        if (v.isEnabled()) {
          v.validate();
        }
      }
      if (eval(_this.condition) && !prmDontHandle) {
        window.setTimeout(_this.callHandlers, 0);
      }
      return (_this.invalidities == 0);
    };
    
    /*Returns all validations.*/
    this.getValidations = function(){
      return _this.validations;
    };
    this.setValidations = function(validations){
      _this.validations=validations;
    };
    
    /*Returns validations which have condition==true.*/
    this.getConditionedValidations = function(){
      var condValidations = [];
      for (var i = 0; i < _this.validations.length; i++) {
        var currentValidation = _this.validations[i];
        if (currentValidation.condition == true || currentValidation.condition == "true" || eval(currentValidation.condition) == true) {
          condValidations.push(currentValidation);
        }
      }
      return condValidations;
    };
    
    /*Returns all the error messages for a validated control as an array. For a valid control should return an empty array.*/
    this.getMessages = function(){
      var r = [];
      for (var i = 0, l = _this.validations.length; i < l; i++) {
        r.push(_this.validations[i].message.evalExpressions());
      }
      return r;
    };
    /*Calls the user defined handler functions defined in the handlers property*/
    this.callHandlers = function(){
      jQuery(_this.handlers).each(function(i){
        //console.log('*** Calling handler['+i+']:'+this.fnc.toString().substring(0,35));
        this.fnc(_this, this.params);
      });
    };
    /*Bind new validations to a HTMLElement object
     @param prmMessage: the error message to be displayed in case of invalidity.Type=String.Mandatory=yes;
     @param prmMinValue: the minimum allowed value. In case of Strings or regex-es, refers to the length of the input, in other cases to the value.
     @param prmMaxValue: the maximum allowed value. In case of Strings or regex-es, refers to the length of the input, in other cases to the value.
     @param prmPattern: the date format pattern(eg.:"yyyy-mm-dd").
     @param prmRegex:the regular expression object (and not only its pattern string).Eg:/^\d{2}:\d{2}$/gi for a time type expression,like 12:35. Type=String. Mandatory=yes.
     @param prmCondition:The boolean condition (must evaluate to true/false) for the validation. While this evaluates to true, the validation is applied, otherwise it will not.
     */
    addVld = function(prmVld){
      var v = new Validation(extend(prmVld, {
        id: _this.id
      }));
      _this.validations.push(v);
      return v;
    };
    this.addNotnull = function(v){
      return addVld(extend(v, {
        fnc: _validateNotnull
      }));
    };
    this.addNotempty = function(v){
      return addVld(extend(v, {
        fnc: _validateNotempty
      }));
    };
    this.addNospaces = function(v){
      return addVld(extend(v, {
        fnc: _validateNospaces
      }));
    };
    this.addMinvalue = function(v){
      return addVld(extend(v, {
        fnc: _validateLimvalue,
        lim: 'min',
        typed: true
      }));
    };
    this.addMaxvalue = function(v){
      return addVld(extend(v, {
        fnc: _validateLimvalue,
        lim: 'max',
        typed: true
      }));
    };
    this.addInt = function(v){
      return addVld(extend(v, {
        fnc: _validateInt,
        typed: true
      }));
    };
    this.addFloat = function(v){
      return addVld(extend(v, {
        fnc: _validateFloat,
        typed: true
      }));
    };
    this.addDecimal = function(v){
      return addVld(extend(v, {
        fnc: _validateDecimal,
        typed: true
      }));
    };
    this.addDate = function(v){
      return addVld(extend(v, {
        fnc: _validateDate,
        typed: true
      }));
    };
    this.addRegex = function(v){
      return addVld(extend(v, {
        fnc: _validateRegex
      }));
    };
    this.addEqualsto = function(v){
      return addVld(extend(v, {
        fnc: _validateEqualsto
      }));
    };
    this.addValues = function(v){
      return addVld(extend(v, {
        fnc: _validateValues
      }));
    };
    this.addIntervals = function(v){
      return addVld(extend(v, {
        fnc: _validateIntervals
      }));
    };
    this.addCustom = function(v){
      return addVld(extend(v, {
        fnc: _validateCustom
      }));
    };
    
    /*Validation object*/
    /*validation object
     @param prmIndex:The internal index of the validation  object (eg. a not_null type validation) within the validations array of
     the control to which the validation is bound to.
     @param id:The id attribute of the HTML element to which the validation is bound to.
     @param fnc: The name of the validation function which gets called by this validation object (eg:validateNotnull).
     @param msg: The error message for the current validation.
     @param min:The minimum allowed value for the control (used in case of minVal or minLength type validations)
     @param max:The maximum allowed value for the control (used in case of maxVal or maxLength type validations)
     @param format:The date format pattern for a control (used only in case of date validations).
     @param regex:The pattern string of the regular expression (used only in case of regular expression-type validations).
     @param condition:The condition for the validation. The validation will be executed only if this condition evaluates to true.
     @param lim:which limit is to be checked ('min' or 'max')
     */
    var defV = {
      id: null,
      fnc: null,
      msg: "",
      min: null,
      max: null,
      format: "yyyy.mm.dd",
      regex: null,
      condition: true,
      lim: null,
      typed: false,
      values: [],
      intervals: [],
      neg: false,
      custom: null
    };
    function Validation(prmV){
      var vld = extend(defV, prmV);
      var _this = this;
      this.ctl = validatedControls[vld.id];
      this.id = vld.id;
      this.fnc = vld.fnc;
      this.min = vld.min;
      this.max = vld.max;
      this.lim = vld.lim;
      this.typed = vld.typed;
      this.format = vld.format;
      this.msg = vld.msg;
      this.regex = vld.regex;
      this.condition = vld.condition;
      this.values = vld.values;
      this.intervals = vld.intervals;
      this.neg = vld.neg;
      this.custom = vld.custom;
      this.executed = false;
      this.isEnabled = function(){
        return (eval(_this.condition) == true)
      };
      try {
        var cond = eval(this.condition);
        if (!(cond == true || cond == false)) 
          throw ""
      } 
      catch (err) {
        alert(fmt(m[2], [this.id, this.fnc]));
        return false;
      }
      this.isValid = null; //null means:unexecuted yet
      this.objValue = null;
      if (this.fnc == _validateNotnull || this.fnc == _validateNotempty) 
        this.ctl.nullValidation = this;
      if (this.fnc == _validateCustom) 
        this.ctl.hasCustom = true;
      this.ctl.allowNull = !(this.ctl.nullValidation && eval(this.ctl.nullValidation.condition) == true);
      this.ctl.req = !this.ctl.allowNull;
      this.val = function(o){
        return validatedControls[o.id].hint == null ? o.value : "";
      };
      function getArg(){
        switch (_this.fnc) {
          case _validateCustom:
            ;
          case _validateDate:
            ;
          case _validateDecimal:
            ;
          case _validateEqualsto:
            ;
          case _validateIntervals:
            ;
          case _validateLimvalue:
            ;
          case _validateRegex:
            return _this;
          case _validateValues:
            return _this;
          default:
            return _this.val(_this.ctl.element);
        }
      };
      this.store = function(isVld, v){
        with (_this) {
          isValid = isVld;
          if (isVld==false) 
            ctl.invalidities++;
          objValue = v;
          executed = true;
        }
        return isVld;
      };
      this.validate = function(){
        var v = _this.val(_this.ctl.element);
        var isValid = (_this.ctl.allowNull && isNull(v) && !_this.ctl.hasCustom) || ((_this.ctl.isTypeCorrect || !_this.typed) && _this.fnc(getArg()));
        var neg = exp(_this, 'neg');
        if (neg.valid && neg.val == true) 
          isValid = !isValid;
        return _this.store(isValid, v);
      };
      return this;
    };/*End of Validation object*/
      };//End of validatedControl object;
  /*ValidationGroup object*/
  function ValidationGroup(ctls){
    this.controls = [];
    //constructor calls
    if (ctls && ctls.length > 0) 
      this.addControls(ctls);
  };/*End of ValidationGroup object*/
  ValidationGroup.prototype = {
    addControls: function(newControls){
      this.controls = this.controls.concat(newControls);
      return this;
    },
    validate: function(){
      var allValid = true;
      jQuery(this.controls).each(function(){
        allValid = this.validate() && allValid;
      });
      return allValid;
    },
    isValid: function(){
      var v = true;
      jQuery(this.controls).each(function(){
        if (this.validate(true) === false) {
          v = false;
          return false;
        }
      });
      return v;
    }
  };
  //Will validate all types of objects:controls,groups,and named groups.
  function _validateAllObjects(arrControls, arrGroups, arrGroupNames){
    var allControlsValid = true, allGroupsValid = true;
    allNamedGroupsValid = true;
    allControlsValid = _validateControls(arrControls);
    allGroupsValid = _validateGroups(arrGroups);
    allNamedGroupsValid = _validateNamedGroups(arrGroupNames);
    return (allControlsValid == true && allGroupsValid == true && allNamedGroupsValid==true);
  };
  //validates the specified controls
  function _validateControls(arrControls){
    var allValid = true;
    for (var i = 0, l = arrControls.length; i < l; i++) {
      allValid = arrControls[i].validate() && allValid;
    }
    return allValid;
  };
  //validates the specified groups
  function _validateGroups(arrGroups){
    var allValid = true;
    for (var i = 0, l = arrGroups.length; i < l; i++) {
      allValid = (arrGroups[i].validate() == true) && (allValid == true);
    }
    return allValid;
  };
  //validates the specified named groups
  function _validateNamedGroups(arrGroupNames){
    var grpNames = arrGroupNames || namedGroups; //if no groups are specified, validate all of them
    var allValid = true;
    for (var i = 0, l = grpNames.length; i < l; i++) {
      if (namedGroups[grpNames[i]]) 
        allValid = namedGroups[grpNames[i]].validate() && allValid;
    }
    return allValid;
  };
  function _areNamedGroupsValid(arrGroupNames){
    for (var i = 0, l = arrGroupNames.length; i < l; i++) {
      if (namedGroups[arrGroupNames[i]].isValid() == false) {
        return false
      };
          }
    return true;
  };
  /*Returns the current version of the Validator object*/
  function _getVersion(){
    return version;
  }
  /*Creates a new ValidationGroup object and returns it*/
  function _createGroup(ctls){
    var newGroupId = getUID(5);
    controlGroups[newGroupId] = new ValidationGroup(ctls);
    return controlGroups[newGroupId];
  };
  //returns a given named group.
  function _getNamedGroup(prmGroupName){
    if (!namedGroups[prmGroupName]) 
      namedGroups[prmGroupName] = new ValidationGroup();
    return namedGroups[prmGroupName];
  };
  /*Returns wether the handlers is specified correctly.*/
  function checkHandlers(hndls){
    if (!hndls || hndls.constructor != Array) 
      return false;
    var h;
    for (var i = 0; i < hndls.length; i++) {
      h = hndls[i];
      if (typeof h["fnc"] == 'undefined' || typeof h["params"] == 'undefined') 
        return false;
    }
    return true;
  };
  function extend(src, over){
    var r = {};
    for (prop in src) 
      r[prop] = src[prop];
    for (prop in over) 
      r[prop] = over[prop];
    return r;
  };
  /*Adds a new ValidatedControl object to the Validator object
   @param id: The HTML id attribute of the HTML element to be validated. Type:String.Mandatory=yes.
   @param type: The type of the data to be validated. Can be one of the following:string,int,float,date. Mandatory=no. Default=string.
   @param event: The name of the html event which will trigger the validation function (eg:onblur). Mandatory=no. Default=onblur.
   @param handlers: The chain of names of the error processing functions, separated by semicolon (eg.:"showRedcross;showInPopup").
   Mandatory=no Default=showRedcross;
   @param order:Defines the order in which the original functions (i any) already bound to the HTMLElement before, and the newly
   bound validation functions will be executed. Possible values:("before","after","instead"), meaning that the newly bound functions will be
   executed before,after or instead of the originally bound functions.
   */
  var defControl = {
    id: null,
    type: 'string',
    events: [],
    handlers: [],
    order: 'after',
    condition: true
  };
  function _addControl(prmControl){
    return (function($){
      var ctl = extend(defControl, prmControl);
      var elem = document.getElementById(ctl.id);
      var evs = ctl.events;
      if (!elem) {
        alert(fmt(m[0], [ctl.id]));
        return false
      }
      if (!ctl.type.isInSet(["string", "int", "float", "date"])) {
        alert(fmt(m[3], [ctl.type, ctl.id]));
        return false
      }
      if (!ctl.order.isInSet(["before", "after", "instead"])) {
        alert(fmt(m[5], [ctl.order, "before,instead,after"]));
        return false
      }
      if (ctl.handlers && !checkHandlers(ctl.handlers)) {
        alert(fmt(m[4], [ctl.id]));
        return false
      }
      try {
        var cond = eval(ctl.condition);
        if (!(cond == true || cond == false)) 
          throw ""
      } 
      catch (err) {
        alert(fmt(m[2], [ctl.id, cond]));
        return false
      }
      /*Input parameters are checked.Hook the validation handler event*/
      if (evs) {
        for (var i = 0; i < evs.length; i++) {
          if(/^on/.test(evs[i])){
            evs[i]=evs[i].replace(/^on/,'');
          }
          $(elem).bind(evs[i],_validate);
          //elem[evs[i]] = _validate;
        }
      }
      /*Register the new control object in the validatedControls MAP*/
      var vldControl = new validatedControl(ctl);
      validatedControls[ctl.id] = elem.ctl = vldControl;
      return vldControl;
    })(jQuery);
  };/*End of _addControl function*/
  /**
   *
   * @param {Object} v
   * @param {Object} lim
   * @param {Object} mode
   */
  function _addControls(prmControl){
    if (!prmControl || !prmControl.id || !(prmControl.id instanceof Array)) {
      return;
    }
    var ctl = extend(defControl, prmControl);
    var ctls = [];
    for (var i = 0, ids = ctl.id; i < ids.length; i++) {
      ctls.push(_addControl({
        id: ids[i],
        type: ctl.type,
        events: ctl.events,
        handlers: ctl.handlers,
        order: ctl.order,
        condition: ctl.condition
      }));
    }
    return ctls;
  };
  /*Stores the results of the current validation in the appropiate validation object*/
  function exp(v, lim, mode){
    var isValid = true;
    try {
      var limVal = (/^exp:/.test(v[lim]) ? eval(v[lim].replace(/^exp:/, "")) : v[lim]);
    } 
    catch (err) {
      alert(fmt(m[7], [v.ctl.id, v[lim]]));
      isValid = false
    }
    if (mode == "num") {
      try {
        if (isNaN(limVal)) 
          throw "";
      } 
      catch (err) {
        alert(fmt(m[6], [v.ctl.id, v[lim]]));
        isValid = false
      }
    }
    return {
      val: limVal,
      valid: isValid
    };
  };
  //validation functions
  function _validateNotnull(v){
    return !isNull(v);
  };
  function _validateNotempty(v){
    return !/^\s*$/.test(v);
  };
  function _validateNospaces(v){
    return !/^\s+$/.test(v);
  };
  function _validateInt(v){
    return isInt(v);
  };
  function _validateFloat(v){
    return isFloat(v);
  };
  function _validateDecimal(vld){
    var v = vld.val(vld.ctl.element), id = exp(vld, 'min', 'num'), fd = exp(vld, 'max', 'num');
    return (id.valid && fd.valid && eval("/^[-+]?\\d{" + id.val + "}(\\.\\d{" + fd.val + "})?$/gi").test("" + v));
  };
  function _validateDate(vld){
    var v = vld.val(vld.ctl.element);
    return (v.toDate(vld.format) != null);
  };
  function _validateRegex(vld){
    var v = vld.val(vld.ctl.element);
    return vld.regex.test(v);
  };
  function _validateEqualsto(vld){
    var v = vld.val(vld.ctl.element);
    return (v == exp(vld, 'min').val);
  };
  function _validateValues(vld){
    var v = vld.val(vld.ctl.element);
    return v.isInSet(vld.values);
  };
  function _validateIntervals(vld){
    var v = vld.val(vld.ctl.element);
    var x = vld.intervals;
    for (var o in x) {
      if (typeof x[o]['min'] == 'undefined') {
        continue;
      }
      if (v >= x[o].min && v <= x[o].max) 
        return true;
    }
    return false;
  };
  function _validateCustom(vld){
    return vld.custom(vld);
  };
  function _validateLimlength(vld){
    var v = vld.val(vld.ctl.element), l = exp(vld, vld.lim, 'num');
    return (l.valid && (vld.lim == 'min' ? (("" + v).length >= l.val) : (("" + v).length <= l.val)));
  };
  function _validateLimint(vld){
    var v = vld.val(vld.ctl.element), l = exp(vld, vld.lim, 'num');
    return (l.valid && (vld.lim == 'min' ? toInt("" + v) >= l.val : toInt("" + v) <= l.val));
  };
  function _validateLimfloat(vld){
    var v = vld.val(vld.ctl.element), l = exp(vld, vld.lim, 'num');
    return (l.valid && (vld.lim == 'min' ? toFloat("" + v) >= l.val : toFloat("" + v) <= l.val));
  };
  var _validateLimnumber = _validateLimfloat;
  function _validateLimdate(vld){
    var v = vld.val(vld.ctl.element);
    var limDateStr = exp(vld, vld.lim).val;
    var ld = limDateStr.toDate(vld.format), od = v.toDate(vld.format);
    if (ld && od) {
      return (vld.lim == 'min' ? od >= ld : od <= ld);
    }
    else {
      return false
    }
  };
  function _validateLimvalue(vld){
    var t = vld.ctl.type;
    switch (t) {
      case 'int':{
        return _validateLimint(vld)
      }
      case 'float':{
        return _validateLimfloat(vld)
      }
      case 'date':{
        return _validateLimdate(vld)
      }
      default:
        {
          return _validateLimlength(vld)
        }
    }
  };
  function _getRunHandleOnBind(){
    return runHandleOnBind
  };
  function _setRunHandleOnBind(v){
    runHandleOnBind = v
  };
  function _getValidatedControlByHtmlId(prmHtmlId){
    return validatedControls[prmHtmlId];
  };
  function _callAllHandlers(){
    for (var i in validatedControls) {
      if (typeof validatedControls[i]['callHandlers'] != 'undefined') {
        window.setTimeout(validatedControls[i].callHandlers, 0);
      }
    }
  };
  /**
   * Explicitely calls the validation for the specified control with a specified
   * handler function. It is posible to pass parameters to the handler function
   * in the paramsArray
   * @param prmControlId: The HTML id attribute of the HTML element to be validated. Type:String.Mandatory=yes.
   * @param prmHandlerFunction: The JS handler function to be called after the validation is called. Type: Function. Mandatory:yes.
   * @param paramsArray: parameters of any type to be passed to the handler function.
   */
  function _validateControlWithHandler(prmControlId, prmHandlerFunction, prmHandlerParams){
    var ctl = validatedControls[prmControlId];
    if (ctl == null || prmHandlerFunction == null) 
      return;
    ctl.validate(true);
    prmHandlerFunction(ctl, prmHandlerParams);
  };
  
  //helper functions. These are used internally all over the code of Validator object
  String.prototype.trim = function(){
    return this.replace(/^\s+|\s+$/g, "")
  };
  String.prototype.trimLeadingZero = function(){
    return this.replace(/^([0]{1})/, "")
  };
  String.prototype.trimLeadingZeros = function(){
    return this.replace(/^([0]+)/, "")
  };
  String.prototype.isInSet = function(arrSet){
    for (var i = 0, l = arrSet.length; i < l; i++) 
      if (this == arrSet[i]) 
        return true;
    return false
  };
  String.prototype.evalExpressions = function(){
    var ret = this, a = this.match(/exp:[.\w]+/gi);
    if (a != null) 
      for (var i = 0, l = a.length; i < l; i++) 
        ret = ret.replace(a[i], eval(a[i].replace(/^(exp:)/, "")));
    return ret;
  };
  function isNull(v){
    return v == null || typeof(v) == "undefined" || ("" + v).length == 0
  };
  function isEmpty(v){
    return /^\s*$/.test("" + v)
  };
  function isZero(v){
    return /^[0]+$/.test(("" + v).trim())
  };
  function isNz(v){
    return isNull(v) || isZero(v)
  };
  function isLeapYear(prmYear){
    return (((prmYear % 4 == 0) && (prmYear % 100 != 0)) || (prmYear % 400 == 0))
  };
  function isInt(v){
    return /^[+-]?\d+$/.test("" + v)
  };
  function isFloat(v){
    return /^[+-]?\d+(.\d+)?$/.test("" + v)
  };
  function toInt(v){
    return parseInt((/^-/.test("" + v) ? "-" : "") + ("" + v).replace(/\D/g, ""))
  };
  function toFloat(v){
    var p = v.split("."), ip = p[0], fp = (p.length > 1 ? "." + p[1] : "");
    return parseFloat(toInt(ip) + fp)
  };
  function nvln(inputVal, valueIfNull){
    return (isEmpty(inputVal) || isNaN(inputVal) ? valueIfNull : inputVal)
  };
  function fmt(msg, arrParams){
    for (var i = 0; i < arrParams.length; i++) 
      msg = msg.replace(eval("/\\(" + i + "\\)/g"), arrParams[i]);
    return msg
  };
  function getUID(len){
    var retVal = [];
    for (var i = 1; i <= len; i++) 
      retVal.push(String.fromCharCode(Math.floor(65.5 + 25 * Math.random())));
    return retVal.join("")
  };
  //tells if a date is valid (against a specified format)
  String.prototype.toDate = function(prmPattern){
    var pattern = (prmPattern || "yyyy-mm-dd").toLowerCase();
    var arrSep = pattern.match(/[^ymd]/gi);
    if (arrSep == null || arrSep.length != 2 || arrSep[0] != arrSep[1]) {
      return null;
    }
    var sep = arrSep[0];
    if (!sep.isInSet(["-", ".", "/"])) {
      return null;
    }
    var arrPatternParts = pattern.split(sep);
    if (arrPatternParts.length != 3) {
      return null;
    }
    var arrDateParts = [], re, reP = "", year = 0, month = 0, day = 0, yearIdx = 0, monthIdx = 0, dayIdx = 0;
    var arrMonths = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
    pattern = pattern.replace(/[^ymd]/gi, "");
    var s = "\\" + sep, y2 = "([0-9]{2})", y4 = "((19|20)[0-9]{2})", m = "(0[1-9]|1[012])", d = "(0[1-9]|[12][0-9]|3[01])";
    switch (pattern) {
      case "yyyymmdd":{
        reP = "^" + y4 + s + m + s + d + "$";
        yearIdx = 0;
        monthIdx = 1;
        dayIdx = 2;
        break;
      }
      case "ddmmyyyy":{
        reP = "^" + d + s + m + s + y4 + "$";
        yearIdx = 2;
        monthIdx = 1;
        dayIdx = 0;
        break;
      }
      case "mmddyyyy":{
        reP = "^" + m + s + d + s + y4 + "$";
        yearIdx = 2;
        monthIdx = 0;
        dayIdx = 1;
        break;
      }
      case "yymmdd":{
        reP = "^" + y2 + s + m + s + d + "$";
        yearIdx = 0;
        monthIdx = 1;
        dayIdx = 2;
        break;
      }
      case "ddmmyy":{
        reP = "^" + d + s + m + s + y2 + "$";
        yearIdx = 2;
        monthIdx = 1;
        dayIdx = 0;
        break;
      }
      case "mmddyy":{
        reP = "^" + m + s + d + s + y2 + "$";
        yearIdx = 2;
        monthIdx = 0;
        dayIdx = 1;
        break;
      }
    }
    re = new RegExp(reP, "gi");
    if (!re.test(this)) 
      return null;
    arrDateParts = this.split(sep);
    year = arrDateParts[yearIdx];
    month = arrDateParts[monthIdx];
    day = arrDateParts[dayIdx];
    if (day > arrMonths[month - 1] + (month == 2 && isLeapYear(year) ? 1 : 0)) 
      return null;
    var d = new Date();
    with (d) {
      setYear(year);
      setMonth(month - 1);
      setDate(day);
      setHours(0);
      setSeconds(0);
      setMilliseconds(0);
    }
    return d;
  };
  //End of helper functions
  var p = Validator.prototype;
  //The PUBLIC methods (callable from outside) of the Validator object:
  
  p.validate = _validate;
  /**Adds a validated control to the validator.*/
  p.addControl = _addControl;
  /**Adds more validated controls to the validator in one shot.*/
  p.addControls = _addControls;
  /**Validates all the enumerated controls.*/
  p.validateControls = _validateControls;
  /**Validates all the enumerated groups.*/
  p.validateGroups = _validateGroups;
  /**Validates all the enumerated named groups.*/
  p.validateNamedGroups = _validateNamedGroups;
  /**Validates all enumerated controls, groups and named groups.*/
  p.validateAllObjects = _validateAllObjects;
  /**returns the Validated control object if its HTML id attribute is specified*/
  p.getValidatedControlByHtmlId = _getValidatedControlByHtmlId;
  /**Returns the namedGroup object byt its name.*/
  p.getNamedGroup = _getNamedGroup;
  /**verifies if all the named groups are valid*/
  p.areNamedGroupsValid = _areNamedGroupsValid;
  /**returns/sets whether the handler functions of controls are automatically called
  right after validations are bound.*/
  p.getRunHandleOnBind = _getRunHandleOnBind;
  p.setRunHandleOnBind = _setRunHandleOnBind;
  /**calls all the handler functions for a control.*/
  p.callAllHandlers = _callAllHandlers;
  /**calls the validation of a specified control with a specified handler function.*/
  p.validateControlWithHandler = _validateControlWithHandler;
  /**Creates a new Validation group of given controls.*/
  p.createGroup = _createGroup;
  /**returns the current version of the validator object.*/
  p.getVersion = _getVersion;
  /**Resets controls and groups*/
  p.reset=_reset;
  /**Sets default handlers, so it does not have to ve set per control.*/
  p.setDefaultHandlers = function(arrHandlers){
    defControl.handlers = arrHandlers;
  };
}//End of Validator Constructor;
