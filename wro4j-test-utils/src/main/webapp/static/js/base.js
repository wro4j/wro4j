/**
 * Javascript counterparts of Tapestry classes.
 * @author elemer.zagoni@isdc.ro
 * @author ionut.david@isdc.ro
 * @author ovidiu.hurducas@isdc.ro
 * @author constantin.partac@isdc.ro
 * @author nicoleta.chindris@isdc.ro
 */
var TA = window.TA ||
{
  /**
   * Generic methods.
   * @author elemer.zagoni
   */
  removeEditor: function(){
    (function($){
      $('#DOMWindowOverlay').remove();
      $('#DOMWindow').remove();
    })(jQuery);
  },
  hideEditor: function(){
    (function($){
      $('#DOMWindowOverlay').css({
        display: 'none'
      });
      $('#DOMWindow').css({
        display: 'none'
      });
    })(jQuery);
  },
  /**
   * Border component.
   * @author constantin.partac@isdc.ro
   */
  Border: {
    onAfterRender: function(){
      (function($){
        var ctx = $("#footer-navigation");
        $("a[rel=lightbox]", ctx).openDOMWindow({
          width: 635,
          height: 440,
          eventType: 'click',
          loader: 1,
          loaderHeight: 16,
          loaderWidth: 17,
          windowSource: 'ajax'
        });
      })(jQuery);
    }
  },
  /**
   * RegisterBrokerOfficeDetails component.
   * @author ionut.david@isdc.ro
   * @author elemer.zagoni@isdc.ro
   */
  RegisterBrokerOfficeDetails: {
    submitBrokerOffice: function(){
      (function($){
        var areAllFieldsValid = validator.validateAllObjects([], [], ["grpRBOD"]);
        validator.callAllHandlers();
        if (!areAllFieldsValid) {
          return;
        }
        $("input[id^=sbmBrokerOffice]").click();
      })(jQuery);
    },
    onAfterRender: function(msg){
      (function($){
        TA.RegisterBrokerOfficeDetails.bindValidations(msg);
      })(jQuery);
    },
    showConfirmationDialog: function(){
      (function($){
        var editorZoneId = "#confirmationDialogContent";
        $.openDOMWindow({
          width: 630,
          height: 160,
          windowSourceID: editorZoneId
        });
      })(jQuery);
    },
    submitConfirmationDialog: function(){
      (function($){
        $.closeDOMWindow({});
        $("#sbmConfirmationDialog").click();
      })(jQuery);
    },
    /**
     * Binds all the necessary validations to this component.
     * @param {Object} msg
     */
    bindValidations: function(msg){
      (function($){
        validator.setDefaultHandlers([{
          fnc: TA.notifyError,
          params: []
        }]);
        
        validator.reset();
        // get context
        var ctx = $('form[id^=frmRegisterBrokerOffice]');
        var grp = validator.getNamedGroup("grpRBOD");
        
        // txtOfficeName:
        var $txtOfficeName = $('input[id^=txtOfficeName]', ctx);
        if ($txtOfficeName.length > 0) {
          var ctlOfficeName = validator.addControl({
            id: $txtOfficeName[0].id,
            events: ['change']
          });
          ctlOfficeName.addNotempty({
            msg: msg['err_officeName_NotEmpty']
          });
          grp.addControls([ctlOfficeName]);
        }
        
        // txtPhoneNumber:
        var $txtPhoneNumber = $('input[id^=txtPhoneNumber]', ctx);
        if ($txtPhoneNumber.length > 0) {
          var ctlPhoneNumber = validator.addControl({
            id: $txtPhoneNumber[0].id,
            events: ['change']
          });
          ctlPhoneNumber.addRegex({
            regex: eval(msg['regex_phoneNumber']),
            msg: msg['err_phoneNumber_Regex']
          });
          grp.addControls([ctlPhoneNumber]);
        }
        
        // txtStreet:
        var $txtStreet = $('input[id^=txtStreet]', ctx);
        if ($txtStreet.length > 0) {
          var ctlStreet = validator.addControl({
            id: $txtStreet[0].id,
            events: ['change']
          });
          ctlStreet.addNotempty({
            msg: msg['err_street_NotEmpty']
          });
          grp.addControls([ctlStreet]);
        }
        
        // txtHouseNumber:
        var $txtHouseNumber = $('input[id^=txtHouseNumber]', ctx);
        if ($txtHouseNumber.length > 0) {
          var ctlHouseNumber = validator.addControl({
            id: $txtHouseNumber[0].id,
            events: ['change']
          });
          ctlHouseNumber.addNotempty({
            msg: msg['err_houseNumber_NotEmpty']
          });
          grp.addControls([ctlHouseNumber]);
        }
        
        // txtZipCode:
        var $txtZipCode = $('input[id^=txtZipCode]', ctx);
        if ($txtZipCode.length > 0) {
          var ctlZipCode = validator.addControl({
            id: $txtZipCode[0].id,
            events: ['change']
          });
          ctlZipCode.addNotempty({
            msg: msg['err_zipCode_NotEmpty']
          });
          ctlZipCode.addRegex({
            regex: eval(msg['regex_zipCode']),
            msg: msg['err_zipCode_Regex']
          });
          grp.addControls([ctlZipCode]);
        }
        
        // txtCity:
        var $txtCity = $('input[id^=txtCity]', ctx);
        if ($txtCity.length > 0) {
          var ctlCity = validator.addControl({
            id: $txtCity[0].id,
            events: ['change']
          });
          ctlCity.addNotempty({
            msg: msg['err_city_NotEmpty']
          });
          grp.addControls([ctlCity]);
        }
        
        // txtAccountNumber:
        var $txtAccountNumber = $('input[id^=txtAccountNumber]', ctx);
        if ($txtAccountNumber.length > 0) {
          var ctlAccountNumber = validator.addControl({
            id: $txtAccountNumber[0].id,
            events: ['change']
          });
          ctlAccountNumber.addNotempty({
            msg: msg['err_accountNumber_NotEmpty']
          });
          ctlAccountNumber.addRegex({
            regex: eval(msg['regex_accountNumber']),
            msg: msg['err_accountNumber_Regex']
          });
          grp.addControls([ctlAccountNumber]);
        }
        
        // txtBcNumber:
        var $txtBcNumber = $('input[id^=txtBcNumber]', ctx);
        if ($txtBcNumber.length > 0) {
          var ctlBcNumber = validator.addControl({
            id: $txtBcNumber[0].id,
            events: ['change']
          });
          ctlBcNumber.addNotempty({
            msg: msg['err_bcNumber_NotEmpty']
          });
          ctlBcNumber.addRegex({
            regex: eval(msg['regex_bcNumber']),
            msg: msg['err_bcNumber_Regex']
          });
          grp.addControls([ctlBcNumber]);
        }
        
        // txtBicNumber:
        var $txtBicNumber = $('input[id^=txtBicNumber]', ctx);
        if ($txtBicNumber.length > 0) {
          var ctlBicNumber = validator.addControl({
            id: $txtBicNumber[0].id,
            events: ['change']
          });
          ctlBicNumber.addRegex({
            regex: eval(msg['regex_bicNumber']),
            msg: msg['err_bicNumber_Regex']
          });
          grp.addControls([ctlBicNumber]);
        }
        
        // txtVatNumber:
        var $txtVatNumber = $('input[id^=txtVatNumber]', ctx);
        if ($txtVatNumber.length > 0) {
          var ctlVatNumber = validator.addControl({
            id: $txtVatNumber[0].id,
            events: ['change']
          });
          ctlVatNumber.addNotempty({
            msg: msg['err_vatNumber_NotEmpty']
          });
          ctlVatNumber.addRegex({
            regex: eval(msg['regex_vatNumber']),
            msg: msg['err_vatNumber_Regex']
          });
          grp.addControls([ctlVatNumber]);
        }
        
        // txtBankName:
        var $txtBankName = $('input[id^=txtBankName]', ctx);
        if ($txtBankName.length > 0) {
          var ctlBankName = validator.addControl({
            id: $txtBankName[0].id,
            events: ['change']
          });
          ctlBankName.addNotempty({
            msg: msg['err_bankName_NotEmpty']
          });
          grp.addControls([ctlBankName]);
        }
        
        // txtChamberOfCommerceNumber:
        var $txtChamberOfCommerceNumber = $('input[id^=txtChamberOfCommerceNumber]', ctx);
        if ($txtChamberOfCommerceNumber.length > 0) {
          var ctlChamberOfCommerceNumber = validator.addControl({
            id: $txtChamberOfCommerceNumber[0].id,
            events: ['change']
          });
          ctlChamberOfCommerceNumber.addNotempty({
            msg: msg['err_chamberOfCommerceNumber_NotEmpty']
          });
          ctlChamberOfCommerceNumber.addRegex({
            regex: eval(msg['regex_chamberOfCommerceNumber']),
            msg: msg['err_chamberOfCommerceNumber_Regex']
          });
          grp.addControls([ctlChamberOfCommerceNumber]);
        }
        
        // txtContactPerson:
        var $txtContactPerson = $('input[id^=txtContactPerson]', ctx);
        if ($txtContactPerson.length > 0) {
          var ctlContactPerson = validator.addControl({
            id: $txtContactPerson[0].id,
            events: ['change']
          });
          ctlContactPerson.addNotempty({
            msg: msg['err_contactPerson_NotEmpty']
          });
          grp.addControls([ctlContactPerson]);
        }
        
        // txtEmail:
        var $txtEmail = $('input[id^=txtEmail]', ctx);
        if ($txtEmail.length > 0) {
          var ctlEmail = validator.addControl({
            id: $txtEmail[0].id,
            events: ['change']
          });
          ctlEmail.addNotempty({
            msg: msg['err_email_NotEmpty']
          });
          ctlEmail.addMinvalue({
            min: msg['minlength_email'],
            msg: msg['err_email_MinLength']
          });
          ctlEmail.addRegex({
            regex: eval(msg['regex_email']),
            msg: msg['err_email_Regex']
          });
          grp.addControls([ctlEmail]);
        }
        
        // chkGeneralConditions - mandatory checked
        var $chkGeneralConditions = $('input[id^=chkGeneralConditions]', ctx);
        if ($chkGeneralConditions.length > 0) {
          var ctlGeneralConditions = validator.addControl({
            id: $chkGeneralConditions[0].id,
            events: ['click'],
            feedbackElement: $chkGeneralConditions.next()
          });
          ctlGeneralConditions.addCustom({
            msg: msg['err_generalConditions_Selected'],
            custom: function(vld){
              return vld.ctl.element.checked == true;
            }
          });
          grp.addControls([ctlGeneralConditions]);
        }
        
        TA.removeEditor();
      })(jQuery);
    }
  },
  /**
   * RegisterGuestDetails component.
   * @author ionut.david@isdc.ro
   * @author elemer.zagoni@isdc.ro
   */
  RegisterGuestDetails: {
    onAfterRender: function(msg){
      (function($){
      
        TA.RegisterGuestDetails.bindValidations(msg);
      })(jQuery);
    },
    /**
     * Binds all the necessary validations to this component.
     * @param {Object} msg
     */
    bindValidations: function(msg){
      (function($){
        validator.setDefaultHandlers([{
          fnc: TA.notifyError,
          params: []
        }]);
        
        validator.reset();
        //get context
        var ctx = $('form[id^=frmRegisterGuest]');
        var grp = validator.getNamedGroup("grpRGD");
        //lstTitle:
        var $lstTitle = $('[id^=lstTitle]', ctx);
        if ($lstTitle.length > 0) {
          var ctlTitle = validator.addControl({
            id: $lstTitle[0].id,
            events: ['change']
          });
          ctlTitle.addNotnull({
            msg: msg['err_title_NotNull']
          });
          grp.addControls([ctlTitle]);
        }
        //txtInitials:
        var $txtInitials = $('input[id^=txtInitials]', ctx);
        if ($txtInitials.length > 0) {
          var ctlInitials = validator.addControl({
            id: $txtInitials[0].id,
            events: ['change']
          });
          ctlInitials.addNotempty({
            msg: msg['err_initials_NotEmpty']
          });
          grp.addControls([ctlInitials]);
        }
        //txtFirstName:
        var $txtFirstName = $('input[id^=txtFirstName]', ctx);
        if ($txtFirstName.length > 0) {
          var ctlFirstName = validator.addControl({
            id: $txtFirstName[0].id,
            events: ['change']
          });
          ctlFirstName.addNotempty({
            msg: msg['err_firstName_NotEmpty']
          });
          grp.addControls([ctlFirstName]);
        }
        //txtLastName:
        var $txtLastName = $('input[id^=txtLastName]', ctx);
        if ($txtLastName.length > 0) {
          var ctlLastName = validator.addControl({
            id: $txtLastName[0].id,
            events: ['change']
          });
          ctlLastName.addNotempty({
            msg: msg['err_lastName_NotEmpty']
          });
          grp.addControls([ctlLastName]);
        }
        //txtHouseNumber:
        var $txtHouseNumber = $('input[id^=txtHouseNumber]', ctx);
        if ($txtHouseNumber.length > 0) {
          var ctlHouseNumber = validator.addControl({
            id: $txtHouseNumber[0].id,
            events: ['change']
          });
          ctlHouseNumber.addNotnull({
            msg: msg['err_houseNumber_NotNull']
          });
          ctlHouseNumber.addRegex({
            regex: eval(msg['regex_houseNumber']),
            msg: msg['err_houseNumber_Regex']
          });
          grp.addControls([ctlHouseNumber]);
        }
        //txtAddress1:
        var $txtAddress1 = $('input[id^=txtAddress1]', ctx);
        if ($txtAddress1.length > 0) {
          var ctlAddress1 = validator.addControl({
            id: $txtAddress1[0].id,
            events: ['change']
          });
          ctlAddress1.addNotempty({
            msg: msg['err_address1_NotEmpty']
          });
          grp.addControls([ctlAddress1]);
        }
        //txtZipCode:
        var $txtZipCode = $('input[id^=txtZipCode]', ctx);
        if ($txtZipCode.length > 0) {
          var ctlZipCode = validator.addControl({
            id: $txtZipCode[0].id,
            events: ['change']
          });
          ctlZipCode.addNotnull({
            msg: msg['err_zipCode_NotNull']
          });
          ctlZipCode.addRegex({
            regex: eval(msg['regex_zipCode']),
            msg: msg['err_zipCode_Regex']
          });
          grp.addControls([ctlZipCode]);
        }
        //txtCity:
        var $txtCity = $('input[id^=txtCity]', ctx);
        if ($txtCity.length > 0) {
          var ctlCity = validator.addControl({
            id: $txtCity[0].id,
            events: ['change']
          });
          ctlCity.addNotempty({
            msg: msg['err_city_NotEmpty']
          });
          grp.addControls([ctlCity]);
        }
        //txtHomePhone
        var $txtHomePhone = $('input[id^=txtHomePhone]', ctx);
        if ($txtHomePhone.length > 0) {
          var ctlHomePhone = validator.addControl({
            id: $txtHomePhone[0].id,
            events: ['change']
          });
          ctlHomePhone.addNotnull({
            msg: msg['err_homePhone_NotNull']
          });
          ctlHomePhone.addRegex({
            regex: eval(msg['regex_homePhone']),
            msg: msg['err_homePhone_Regex']
          });
          grp.addControls([ctlHomePhone]);
        }
        //txtPhone2
        var $txtPhone2 = $('input[id^=txtPhone2]', ctx);
        if ($txtPhone2.length > 0) {
          var ctlPhone2 = validator.addControl({
            id: $txtPhone2[0].id,
            events: ['change']
          });
          ctlPhone2.addRegex({
            regex: eval(msg['regex_mobilePhone']),
            msg: msg['err_mobilePhone_Regex']
          });
          grp.addControls([ctlPhone2]);
        }
        //txtEmailAddress
        var $txtEmailAddress = $('input[id^=txtEmailAddress]', ctx);
        if ($txtEmailAddress.length > 0) {
          var ctlEmailAddress = validator.addControl({
            id: $txtEmailAddress[0].id,
            events: ['change']
          });
          ctlEmailAddress.addMinvalue({
            min: msg['minlength_email'],
            msg: msg['err_email_MinLength']
          });
          ctlEmailAddress.addRegex({
            regex: eval(msg['regex_email']),
            msg: msg['err_email_Regex']
          });
          grp.addControls([ctlEmailAddress]);
        }
        // chkCustomerDb - mandatory checked for DE
        var $chkCustomerDb = $('input[id^=chkCustomerDb]', ctx);
        if ($chkCustomerDb.length > 0) {
          if (msg['country_code'].toUpperCase() == 'DE') {
            var ctlCustomerDb = validator.addControl({
              id: $chkCustomerDb[0].id,
              events: ['click'],
              feedbackElement: $('label[for^=chkCustomerDb]')
            });
            ctlCustomerDb.addCustom({
              msg: msg['err_promotableStatus_Selected_DE'],
              custom: function(vld){
                return vld.ctl.element.checked == true;
              }
            });
            grp.addControls([ctlCustomerDb]);
          }
        }
        TA.removeEditor();
      })(jQuery);
    },
    /**
     * Reloads the RegisterGuestDetails form according to the currently
     * selected country.
     * @param {Object} id
     */
    submitGuest: function(id){
      (function($){
        //when not submitting from combo-change must also be validated:
        if (id == 'sbmRegisterGuest') {
          var areAllFieldsValid = validator.validateAllObjects([], [], ["grpRGD"]);
          validator.callAllHandlers();
          if (!areAllFieldsValid) {
            return;
          }
        }
        $("input[id=" + id + "]").click();
      })(jQuery);
    }
  },
  /**
   * StayDetails component.
   * @author elemer.zagoni@isdc.ro
   * @author ovidiu.hurducas@isdc.ro
   * @author constantin.partac@isdc.ro
   */
  StayDetails: {
    storeSelectedAction: function(){
      (function($){
        $("[id^=CottageDetailsSubmit]").click();
      })(jQuery);
    },
    onContinueClick: function(linkId){
      (function($){
        //Clean up error popups and dialogs.
        $("div[id$=:errorpopup]").remove();
        TA.removeEditor();
      })(jQuery);
    },
    submitInsurances: function(){
      (function($){
        $("input[id^=sbmInsurances]").click();
      })(jQuery);
    },
    onAfterRender: function(){
      (function($){
        var ctx = $("table[id^=overview-table]");
        $("a[rel=lightbox]", ctx).openDOMWindow({
          width: 633,
          height: 370,
          eventType: 'click',
          loader: 1,
          loaderHeight: 16,
          loaderWidth: 17,
          windowSource: 'ajax'
        });
      })(jQuery);
    }
  },
  /**
   * CottageDetails component.
   * @author elemer.zagoni@isdc.ro
   */
  CottageDetails: {
    onAfterRender: function(msg){
      (function($){
        TA.CottageDetails.bindValidations(msg);
      })(jQuery);
    },
    /**
     * Binds all validations to this component.
     */
    bindValidations: function(msg){
      (function($){
        validator.reset();
        validator.setDefaultHandlers([{
          fnc: TA.notifyError,
          params: []
        }, {
          fnc: TA.disableButton,
          params: [$('input[id^=sbmActionCode]')]
        }]);
        var ctx = $('form[id^=frmActionCode]');
        var $txtActionCode = $("input[id^=txtActionCode]");
        //actionCode
        var ctlActionCode = validator.addControl({
          id: $txtActionCode[0].id,
          events: []
        });
        
        ctlActionCode.addNotempty({
          msg: msg['err_actionCode_NotEmpty']
        });
        ctlActionCode.addCustom({
          msg: msg['err_actionCode_Invalid'],
          custom: function(){
            var result = false;
            if (/^\s*$/.test($txtActionCode.val())) {
              return false;
            }
            DWRFacade.getActionByCode($txtActionCode.val(), {
              async: false,
              callback: function(isValidAction){
                result = isValidAction;
              }
            });
            return result;
          }
        });
        
        validator.getNamedGroup("grpCottageDetails").addControls([ctlActionCode]);
        //TODO redirect ENTER key as if it was a click:
        
        $("input[id^=sbmActionCode]", ctx).click(function(){
          return validator.validateAllObjects([], [], ["grpCottageDetails"]);
          validator.callAllHandlers();
        });
        
      })(jQuery);
    },
    /**
     * Event handler for submit Action code button.
     */
    addActionCode: function(){
      (function($){
        $('input[id^=sbmActionCode]').click();
      })(jQuery);
    }
  },
  /**
   * CottageServiceDetails component.
   * @author constantin.partac@isdc.ro
   */
  CottageServicesDetails: {
    onSubmitService: function(){
      (function($){
        $("input[id^=sbmCottageServiceDetails]").click();
      })(jQuery);
    },
    onAfterRender: function(){
      (function($){
      
        $("a[rel=lightbox]").openDOMWindow({
          width: 633,
          height: 370,
          eventType: 'click',
          loader: 1,
          loaderHeight: 16,
          loaderWidth: 17,
          windowSource: 'ajax'
        });
      })(jQuery);
    }
  },
  /**
   * PreferencesDetails component.
   * @author constantin.partac@isdc.ro
   * @author ionut.david@isdc.ro
   */
  PreferencesDetails: {
    any: null,
    msg: null,
    ctx: null,
    /**
     * Called after the component finished to render.
     * @param {Object} msg
     */
    onAfterRender: function(msg){
      (function($){
        TA.PreferencesDetails.any = msg["txt_Any"];
        TA.PreferencesDetails.msg = msg;
        TA.PreferencesDetails.ctx = $('#prefs');
        TA.PreferencesDetails.bindValidations(msg);
      })(jQuery);
    },
    /**
     * binds all validations to preferences.
     * @param {Object} chk
     */
    bindValidations: function(msg){
      (function($){
        validator.reset();
        var grpPrefs = validator.getNamedGroup("grpPrefs");
        validator.setDefaultHandlers([{
          fnc: TA.NotifyGroup,
          params: [TA.PreferencesDetails.ctx, msg]
        }, {
          fnc: TA.notifyError,
          params: [msg]
        }]);
        var allPrefs = $('input:radio,input:checkbox', TA.PreferencesDetails.ctx); //
        var ctls = new Array(allPrefs.length);
        allPrefs.each(function(i){
          ctls[i] = validator.addControl({
            id: this.id,
            events: ['click'],
            feedbackElement: $(this).next()
          });
          ctls[i].addCustom({
            msg: '',
            custom: function(vld){
              var isGroupValid = false;
              DWRFacade.savePreferences(TA.PreferencesDetails.getSelectedPrefs(), {
                async: false,
                callback: function(errorKeys){
                  //alert(JSONstring.make(errorKeys));
                  if (errorKeys.length > 0) {
                    vld.msg = errorKeys;
                  }
                  isGroupValid = (errorKeys.length == 0);
                  if (isGroupValid) {
                    TA.BookingSummary.reload();
                  }
                }
              });
              return isGroupValid;
            }
          });
          grpPrefs.addControls([ctls[i]]);
          var cmbCottageNr = $('[id^=cmbCottageSelection]');
          var ctlCottageNr = validator.addControl({
            id: cmbCottageNr[0].id,
            events: ['change']
          });
          ctlCottageNr.addCustom({
            msg: '',
            custom: function(vld){
              var areAllValid = false;
              TA.PreferencesDetails.disableCheckBoxes(cmbCottageNr.val());
              DWRFacade.reserveCottage(cmbCottageNr.val(), {
                async: false,
                callback: function(errors){
                  vld.msg = errors;
                  areAllValid = errors.length == 0;
                  if (areAllValid) {
                    TA.BookingSummary.reload();
                  }
                }
              });
              return areAllValid;
            }
          });
          
        });
      })(jQuery);
    },
    /**
     * Returns all the selected preferences.
     */
    getSelectedPrefs: function(){
      return (function($){
        var selectedPrefs = [];
        var optionalCount = 0;
        var selectedCheckboxes = $('input:checked', TA.PreferencesDetails.ctx).each(function(){
          if (this.type.toLowerCase() == 'checkbox') {
            optionalCount++;
          }
          selectedPrefs.push({
            code: $(this).attr('code'),
            description: $(this).attr('description'),
            group: $(this).attr('group')
          });
        });
        TA.PreferencesDetails.disableCottageCMB(optionalCount > 0);
        return selectedPrefs;
      })(jQuery);
    },
    /**
     * Sets the enabled/disabled state of the cottage combo.
     * @param {Object} disable
     */
    disableCottageCMB: function(disable){
      (function($){
        if (disable) {
          $("select[id^=cmbCottageSelection]").attr("disabled", "disabled");
        }
        else {
          $("select[id^=cmbCottageSelection]").removeAttr("disabled");
        }
      })(jQuery);
    },
    /**
     * Sets the enable/disable state of the check boxes and radio buttons.
     * @param {Object} val
     */
    disableCheckBoxes: function(val){
      (function($){
        if (val == TA.PreferencesDetails.any) {
          $('#preferencesList input').removeAttr("disabled");
          $("input[id^=rdbFreePreference]").removeAttr("disabled");
        }
        else {
          $('#preferencesList input').attr("disabled", "disabled");
          $("input[id^=rdbFreePreference]").attr("disabled", "disabled");
        }
      })(jQuery);
    }
  },
  /**
   * BookingSummary component.
   * @author elemer.zagoni@isdc.ro
   */
  BookingSummary: {
    reload: function(){
      (function($){
        $("[id^=sbmBookingSummary]").click();
      })(jQuery);
    },
    onAfterRender: function(componentId){
      (function($){
        var ctx = $("#booking-summary");
        $("a", ctx).unbind('click').click(function(){
          $(this).parents('li').eq(0).toggleClass('closed').toggleClass('open');
        });
        $("a[rel=lightbox]", ctx).openDOMWindow({
          width: 633,
          height: 450,
          eventType: 'click',
          loader: 1,
          loaderHeight: 16,
          loaderWidth: 17,
          windowSource: 'ajax'
        });
      })(jQuery);
    }
  },
  /**
   * ArrangementsContainer component.
   * @author elemer.zagoni@isdc.ro
   */
  ArrangementsContainer: {
    onAfterRender: function(){
      (function($){
        $("#StayTabZone .section fieldset.arrangement-item h2").unbind('click').click(function(){
          $(this).parents('fieldset').eq(0).toggleClass('closed').toggleClass('open')
        });
        
      })(jQuery);
    }
  },
  /**
   * ArrangementsItem component.
   * @author elemer.zagoni@isdc.ro
   */
  ArrangementsItem: {
    openEditor: function(itemId){
      (function($){
        var editorZoneId = "#ItemEditorZone-" + itemId;
        $.openDOMWindow({
          width: 630,
          height: 250,
          windowSourceID: editorZoneId
        });
      })(jQuery);
    },
    
    submit: function(itemId){
      (function($){
        $("input[id^=sbmArrangement-" + itemId + "]").click();
        TA.removeEditor();
      })(jQuery);
    }
  },
  /**
   * PersonalInfoDetails component.
   * @author ionut.david@isdc.ro
   */
  PersonalInfoDetails: {
    openWindow: function(){
      (function($){
        var editorZoneId = '#IdentifyBrokerDialogZone';
        $.openDOMWindow({
          width: 630,
          height: 190,
          overlayOpacity: 85,
          borderSize: 0,
          overlayColor: "#fff",
          windowSourceID: editorZoneId
        });
      })(jQuery);
    },
    
    closeWindow: function(){
      TA.removeEditor();
    }
  },
  /**
   * Search details component.
   * @author elemer.zagoni@isdc.ro
   */
  SearchDetails: {
    /**The current language*/
    lng: "de",
    /**Currently used calendar object.*/
    calendar: null,
    /**All the valid selectable dates.*/
    validDates: {},
    /**The minimum valid selectable date*/
    minDate: new Date(),
    /**The minimum valid selectable date*/
    maxDate: new Date(),
    /**The message catalog*/
    msg: null,
    arrivalDates: {},
    /**
     * Called after the component finished to render.
     * @param {String} lng the current language code (eg:en,de,nl)
     */
    onAfterRender: function(lng, msg, arrivalDates){
      (function($){
        eval("TA.SearchDetails.arrivalDates={" + arrivalDates + "}");
        TA.SearchDetails.bindListeners();
        TA.SearchDetails.msg = msg;
        $.unblockUI();
        TA.SearchDetails.lng = lng;
        var duration = $("[name=cmbDuration]").val();
        var objDuration = TA.SearchDetails.arrivalDates[duration];
        if (objDuration) {
          TA.SearchDetails.duration = duration;
          TA.SearchDetails.validDates = objDuration.validSelections;
          TA.SearchDetails.minDate = new Date(objDuration.minDate);
          TA.SearchDetails.maxDate = new Date(objDuration.maxDate);
        }
        
        var calendar = new Calendar(1, null, TA.SearchDetails.onDateSelected, function(cal){
          cal.hide();
        });
        calendar.setDateFormat("%d/%m/%Y");
        calendar.setDateStatusHandler(TA.SearchDetails.isDisabledDate);
        calendar.create(null);
        TA.SearchDetails.calendar = calendar;
        var lngPath = "js/lang/calendar-" + lng + ".js";
        /*
         jQuery.getScript(lngPath, function(data, textStatus){
         });
         */
        TA.SearchDetails.bindValidations($("form[id^=frmSearch]"), msg);
        /**
         * Called after a date inside calendar has been selected (clicked).
         * @param {Object} calendar
         * @param {Object} date
         */
      })(jQuery);
    },
    /**
     * Binds event listeners dinamically.
     */
    bindListeners: function(){
      (function($){
        $('#DatepickerTrigger').click(TA.SearchDetails.onShowCalendar);
        $('a[id^=lnkSearchSubmit]').click(TA.SearchDetails.submitSearchForm);
        $('select[id^=cmbDuration]').change(TA.SearchDetails.onDurationChanged);
        $('select[id^=cmbArrivalMonth]').change(TA.SearchDetails.onArrivalMonthChanged);
        $('select[id^=cmbVillage]').change(TA.SearchDetails.submitSearchDetails);
        $('select[id^=cmbNoOfAdults]').change(TA.SearchDetails.checkNumberOfAdultsChange);
        $('select[id^=cmbNoOfInfants]').change(TA.SearchDetails.checkMaxNumberOfpersons);
        $('select[id^=cmbNoOfChildren]').change(TA.SearchDetails.checkMaxNumberOfpersons);
        $('select[id^=cmbNoOfCottages]').change(TA.SearchDetails.submitSearchDetails);
        
      })(jQuery);
    },
    
    /**
     * Called on change of the duration combo.
     * @param {Object} duration The current duration value.
     */
    onDurationChanged: function(e){
      (function($){
        var duration = $(e.target).val();
        var objDuration = TA.SearchDetails.arrivalDates[duration];
        if (!objDuration) {
          return;
        }
        //reset day combo:
        var cmbArrivalDay = $("[name=cmbArrivalDay]");
        cmbArrivalDay.html("<option value=''>" + Calendar._TT["SELECT_DAY"] + "</option>");
        cmbArrivalDay[0].selectedIndex = 0;
        //refill months combo:
        var months = objDuration.items;
        var options = [];
        
        for (var month in months) {
          options.push("<option value='" + month + "'>" + months[month].label + "</option>");
        }
        var cmbArrivalMonth = $("[name=cmbArrivalMonth]");
        cmbArrivalMonth.html(options.join(""));
        if (cmbArrivalMonth[0].options.length > 0) {
          cmbArrivalMonth[0].selectedIndex = 0;
          cmbArrivalMonth.change();//force repopulation of days
          cmbArrivalMonth.blur();//force re-validation
        }
        TA.SearchDetails.validDates = objDuration.validSelections;
        TA.SearchDetails.minDate = new Date(objDuration.minDate);
        TA.SearchDetails.maxDate = new Date(objDuration.maxDate);
        TA.SearchDetails.duration = duration;
      })(jQuery);
    },
    
    /**
     * Called when arrival month is changed.
     * @param {Object} monthVal
     */
    onArrivalMonthChanged: function(e){
      (function($){
        var monthVal = $(e.target).val();
        var duration = $("[name=cmbDuration]").val();
        var objDuration = TA.SearchDetails.arrivalDates[duration];
        if (!objDuration) {
          return;
        }
        
        var cmbArrivalDay = $("[name=cmbArrivalDay]");
        if (monthVal == "") {
          cmbArrivalDay.html("<option value=''>" + Calendar._TT["SELECT_DAY"] + "</option>");
          cmbArrivalDay[0].selectedIndex = 0;
          cmbArrivalDay.change();
        }
        else {
          var days = objDuration.items[monthVal].days;
          var options = [];
          options.push("<option value=''>" + Calendar._TT["SELECT_DAY"] + "</option>");
          for (var day = 0; day < days.length; day++) {
            options.push("<option value='" + days[day] + "'>" + days[day] + "</option>");
          }
          cmbArrivalDay.html(options.join(""));
          cmbArrivalDay[0].selectedIndex = 0;
          cmbArrivalDay.change();
        }
      })(jQuery);
    },
    
    /**
     * Called before the calendar is displayed.
     */
    onBeforeShowCalendar: function(calendar){
      (function($){
        var strDMY = $("[name=cmbArrivalDay]").val() + "/" + $("[name=cmbArrivalMonth]").val();
        var comboDate = Date.parse(strDMY);
        var defaultDateOpen = (isNaN(comboDate) ? TA.SearchDetails.minDate : strDMY.parseCPDate());
        calendar.setDate(defaultDateOpen);
      })(jQuery);
    },
    
    /**
     * Called when the calendar button is clicked.
     * @param {Object} btn the clicked button.
     */
    onShowCalendar: function(){
      (function($){
        TA.SearchDetails.onBeforeShowCalendar(TA.SearchDetails.calendar);
        TA.SearchDetails.calendar.showAtElement($("#DatepickerTrigger")[0]);
      })(jQuery);
    },
    
    /**
     * Called when a date is selected in the calendar popup.
     * @param {Object} calendar
     * @param {Object} date
     */
    onDateSelected: function(calendar, date){
      return (function($){
        var dmy = date.split("/");
        $("[name=cmbArrivalMonth]").val(dmy[1] + "/" + dmy[2]).change();
        $("[name=cmbArrivalDay]").val(dmy[0]).change();
        if (calendar.dateClicked) {
          calendar.callCloseHandler();
        }
      })(jQuery);
    },
    /**
     * Determines which dates will be disabled (unselectable)
     * inside the calendar.
     * @param {Object} date
     * @param {Object} y
     * @param {Object} m
     * @param {Object} d
     */
    isDisabledDate: function(date, y, m, d){
      return (function($){
        var t = (new Date(y, m, d, 0, 0, 0, 0)).getTime();
        return (TA.SearchDetails.arrivalDates[TA.SearchDetails.duration].validSelections[t] != 1);
      })(jQuery);
    },
    /**
     * Called when searchdetails are submitted.
     */
    submitSearchDetails: function(){
      (function($){
        $("#hdnArrivalMonth").val($("[name=cmbArrivalMonth]").val());
        $("#hdnArrivalDay").val($("[name=cmbArrivalDay]").val());
        $("#hdnFinalSubmit").val("false");
        $('[id^=sbmSearch]').click();
      })(jQuery);
    },
    /**
     * Called when searchform is submitted.
     */
    submitSearchForm: function(){
      (function($){
        //don't let search if any control is invalid:
        var areAllGroupsValid = validator.validateAllObjects([], [], ['grpSearchDetails']);
        validator.callAllHandlers();
        if (!areAllGroupsValid) {
          return;
        }
        var month = $("[name=cmbArrivalMonth]").val();
        var day = $("[name=cmbArrivalDay]").val();
        $("#hdnArrivalMonth").val(month);
        $("#hdnArrivalDay").val(day);
        $("#hdnFinalSubmit").val("true");
        
        $.blockUI({
          message: '<h1><img src="images/TRAVELAGENT/style/bullets/search.png" /> ' + TA.SearchDetails.msg['lbl_Searching'] + '</h1>'
        });
        $('input[id^=sbmSearch]').click();
      })(jQuery);
    },
    
    /**
     * Verify if 'more...' or 'less...' options were selected,
     * if yes then reload the details zone.
     */
    checkNumberOfAdultsChange: function(e){
      (function($){
        var adults = $(e.target).val();
        if (adults == 13 && $(e.target).containsOption("11")) { // can't use 12 because less is also 12
          var cottageSelect = $("select[id^=cmbNoOfCottages]");
          if (cottageSelect.val() == 1) {
            cottageSelect.val(2);
          }
          TA.SearchDetails.submitSearchDetails();
        }
        else 
          if (adults == 12 && $(e.target).containsOption("14")) { // can't use 13 because more is also 13
            TA.SearchDetails.submitSearchDetails();
          }
          else {
            TA.SearchDetails.checkMaxNumberOfpersons(e);
          }
      })(jQuery);
    },
    
    /**
     * Verifies if the total number of persons (babies excluded) per cottage is more than 12.
     * If yes them modifies the selected number of cottages keeping the rule "at least one adult per cottage,
     * also updates the model of cottage select and adults select if necessary by adding or removing options.
     */
    checkMaxNumberOfpersons: function(e){
      (function($){
        var sbmSearchDetails = false;
        var adultsSelect = $("select[id^=cmbNoOfAdults]");
        var cottageSelect = $("select[id^=cmbNoOfCottages]");
        var noOfAdults = parseInt(adultsSelect.val());
        var noOfPersons = noOfAdults;
        noOfPersons += parseInt($("select[id^=cmbNoOfInfants]").val());
        noOfPersons += parseInt($("select[id^=cmbNoOfChildren]").val());
        var noOfCottages = parseInt(cottageSelect.val());
        var minNoOfCottages = Math.ceil(noOfPersons / 12);
        minNoOfCottages = minNoOfCottages > 4 ? 4 : minNoOfCottages;
        if (noOfCottages < minNoOfCottages) {
          noOfCottages = minNoOfCottages;
          sbmSearchDetails = true;
          if (noOfAdults < noOfCottages) {
            noOfAdults = noOfCottages;
          }
        }
        TA.SearchDetails.updateAdultsSelectionModel(noOfCottages, noOfAdults);
        TA.SearchDetails.updateCottageSelectionModel(minNoOfCottages, noOfCottages);
        if (sbmSearchDetails) {
          TA.SearchDetails.submitSearchDetails();
        }
      })(jQuery);
    },
    
    /**
     * Updates the available optios for Cottage Select model.
     * Removes options till minNoOfCottages (exclusive) and adds those between minNoOfCottages (inclusive)
     * up to noOfCottages (exclusive).
     * @param {minNoOfCottages} minimum number of cottages option.
     * @param {noOfCottages} current selected number of cottages option.
     */
    updateCottageSelectionModel: function(minNoOfCottages, noOfCottages){
      (function($){
        var cottageSelect = $("select[id^=cmbNoOfCottages]");
        var updated = false;
        var is;
        for (var i = 1; i < minNoOfCottages; i++) {
          is = String(i);
          if (cottageSelect.containsOption(is)) {
            cottageSelect.removeOption(is);
            updated = true;
          }
        }
        for (i = minNoOfCottages; i < noOfCottages; i++) {
          is = String(i);
          if (!cottageSelect.containsOption(is)) {
            cottageSelect.addOption(is, is, false);
            updated = true;
          }
        }
        if (updated) {
          cottageSelect.sortOptions();
          cottageSelect.val(noOfCottages);
        }
      })(jQuery);
    },
    
    /**
     * Updates the available optios for Adults Select model.
     * If noOfAdults is less than 13 removes options up to minNoOfAdults (exclusive) and adds those
     * between minNoOfAdults (inclusive) up to noOfAdults (exclusive).
     * @param {minNoOfCottages} minimum number of adults option.
     * @param {noOfCottages} current selected number of adults option.
     */
    updateAdultsSelectionModel: function(minNoOfAdults, noOfAdults){
      (function($){
        if (noOfAdults <= 12) {
          var adultsSelect = $("select[id^=cmbNoOfAdults]");
          var updated = false;
          var is;
          for (var i = 1; i < minNoOfAdults; i++) {
            is = String(i);
            if (adultsSelect.containsOption(is)) {
              adultsSelect.removeOption(is);
              updated = true;
            }
          }
          for (i = minNoOfAdults; i < noOfAdults; i++) {
            is = String(i);
            if (!adultsSelect.containsOption(is)) {
              adultsSelect.addOption(is, is, false);
              updated = true;
            }
          }
          if (updated) {
            //adultsSelect[0].options.sort(function(a, b){
            //  return parseInt(a.value) > parseInt(b.value) ? 1 : -1;
            //});
            //adultsSelect.sortOptions();
            adultsSelect.val(noOfAdults);
          }
        }
      })(jQuery);
    },
    
    /**
     * Binds all the client-side validations to controls on a form.
     * @param {Object} $frm The form inside which the validated controls must reside.
     * @param {Object} msg The message catalog (passed from server-side).
     */
    bindValidations: function($frm, msg){
      (function($){
        validator.reset();
        validator.setDefaultHandlers([{
          fnc: TA.notifyError,
          params: []
        }]);
        var $cmbDuration = $("select[id^=cmbDuration]");
        var $cmbArrivalMonth = $("select[id^=cmbArrivalMonth]");
        var $cmbArrivalDay = $("select[id^=cmbArrivalDay]");
        var $cmbVillage = $("select[id^=cmbVillage]");
        
        //duration
        var ctlDuration = validator.addControl({
          id: $cmbDuration[0].id,
          events: ['change']
        });
        ctlDuration.addNotnull({
          msg: msg['err_cmbDuration_Mandatory']
        });
        //arrivalMonth
        var ctlArrivalMonth = validator.addControl({
          id: $cmbArrivalMonth[0].id,
          events: ['change']
        });
        ctlArrivalMonth.addNotnull({
          msg: msg['err_cmbArrivalMonth_Mandatory']
        });
        /*
         ctlArrivalMonth.addCustom({
         msg: msg['err_cmbArrivalMonth_Mandatory'],
         custom:function(vld){return true;}
         });
         ctlArrivalMonth.addCustom({
         msg: msg['err_cmbArrivalMonth_Mandatory'],
         custom:function(vld){vld.isValid=null;return false;}
         });
         */
        //village
        var ctlVillage = validator.addControl({
          id: $cmbVillage[0].id,
          events: ['change']
        });
        ctlVillage.addNotnull({
          msg: msg['err_cmbVillage_Mandatory']
        });
        
        validator.getNamedGroup("grpSearchDetails").addControls([ctlDuration, ctlArrivalMonth, ctlVillage]);
        $("[id^=lnkSearchSubmit]", $frm).click(function(){
          return validator.validateAllObjects([], [], ["grpSearchDetails"]);
        });
      })(jQuery);
    }
  },
  /**
   * The JS support of the AvailabilityDetails component.
   */
  AvailabilityDetails: {
    onAfterRender: function(){
      (function($){
        $.unblockUI();
      })(jQuery);
    }
  },
  /**
   * The JS support of the BookingResults component.
   * @author constantin.partac@isdc.ro
   */
  BookingResults: {
    onAfterRender: function(){
      (function($){
        var ctx = $("ul[id^=ulBookingResults]");
        $("a[rel=lightbox]", ctx).openDOMWindow({
          width: 633,
          height: 450,
          eventType: 'click',
          loader: 1,
          loaderHeight: 16,
          loaderWidth: 17,
          windowSource: 'ajax'
        });
      })(jQuery);
    }
  },
  /**
   * IdentifyBrokerInfo component.
   * @author ionut.david@isdc.ro
   */
  IdentifyBrokerInfo: {
    brokerOffice: null,
    onAfterRender: function(msg){
      (function($){
        TA.IdentifyBrokerInfo.bindValidations($("form[id^=frmIdentifyBroker]"), msg);
      })(jQuery);
    },
    closeDialog: function(){
      (function($){
        $.closeDOMWindow({
          functionCallOnClose: true,
          functionCallBeforeClose: function(){
          },
          functionCallAfterClose: function(){
            $('#IdentifyBrokerDialog').append($("#DOMWindowContent").children());
          }
        });
      })(jQuery);
    },
    /**
     * Binds validations to IdentifyBrokerInfo component.
     * @param {Object} $frm
     * @param {Object} msg
     */
    bindValidations: function($frm, msg){
      (function($){
        validator.reset();
        validator.setDefaultHandlers([{
          fnc: TA.notifyError,
          params: []
        }, {
          fnc: TA.disableButton,
          params: [$('input[id^=sbmBrokerNr]')]
        }]);
        var $txtBrokerNumber = $("input[id^=txtBrokerNumber]");
        
        //BrokerNumber
        var ctlBrokerNumber = validator.addControl({
          id: $txtBrokerNumber[0].id,
          events: ['change']
        });
        ctlBrokerNumber.addNotempty({
          msg: msg['err_BrokerOffice_NotEmpty']
        });
        ctlBrokerNumber.addCustom({
          msg: msg['err_BrokerOffice_Invalid'],
          custom: function(vld){
            DWRFacade.getBrokerOfficeByNumber($('input[id^=txtBrokerNumber]').val(), {
              async: false,
              callback: function(brokerOffice){
                TA.IdentifyBrokerInfo.brokerOffice = brokerOffice;
              }
            });
            return (TA.IdentifyBrokerInfo.brokerOffice != null);
          }
        });
        $txtBrokerNumber.keydown(function(event){
          switch (event.keyCode) {
            case 13:{
              event.preventDefault();
              return false;
            }
          }
        });
        validator.getNamedGroup("grpIdentifyBrokerInfo").addControls([ctlBrokerNumber]);
      })(jQuery);
    },
    /**
     *
     */
    onIdentifyClick: function(){
      (function($){
        var allValid = validator.validateAllObjects([], [], ['grpIdentifyBrokerInfo']);
        validator.callAllHandlers();
        if (allValid) {
          var ctx = $('#IdentifyBrokerDialog');
          var brokerOffice = TA.IdentifyBrokerInfo.brokerOffice;
          $('#brokerOfficeName', ctx).text(brokerOffice.name);
          $('#brokerOfficeCity', ctx).text(brokerOffice.city);
          $('#brokerOfficeZipCode', ctx).text(brokerOffice.zipCode);
          $.openDOMWindow({
            width: 640,
            height: 200,
            windowSourceID: '#IdentifyBrokerDialog',
            functionCallOnOpen: function(){
              $('a[id^=lnkIdentifyBrokerInfo]').focus();
            }
          });
        }
      })(jQuery);
    },
    /**
     *
     * @param {Object} vld
     */
    isBrokerValid: function(vld){
      return (function($){
      })(jQuery);
    },
    /**
     *
     */
    onConfirmClick: function(){
      (function($){
        $.closeDOMWindow({});
        $("input[id^=sbmBrokerNr]").click();
      })(jQuery);
    }
  },
  /**
   * PaymentDetails component.
   * @author nicoleta.chindris@isdc.ro
   */
  PaymentDetails: {
    onCreditCardState: function(isEnabled){
      (function($){
        if (isEnabled) {
          $("select,input[type=text]", "fieldset.credit-card-information").removeAttr("disabled");
        }
        else {
          $("select,input[type=text]", "fieldset.credit-card-information").attr("disabled", "disabled");
        }
      })(jQuery);
    },
    onSubmitClick: function(){
      (function($){
        $("input[id^=btnSubmitPayment]").click();
      })(jQuery);
    },
    /**
     * Called when ogone form is submitted.
     */
    submitOgoneForm: function(){
      (function($){
        $("input[id^=btnSubmitOgoneForm]").click();
      })(jQuery);
    }
  },
  /**
   * Insurances compoenent from inside booking overview (BookingDetails) component.
   * @author elemer.zagoni@isdc.ro
   * @author constantin.partac@isdc.ro
   */
  Insurances: {
    onAfterRender: function(){
      (function($){
        var ctx = $("form[id^=frmInsurances]");
        $('a[rel=lightbox]').openDOMWindow({
          modal: 1,
          width: 633,
          height: 450,
          eventType: 'click',
          loader: 1,
          loaderHeight: 16,
          loaderWidth: 17,
          windowSource: 'ajax'
        });
      })(jQuery);
    }
  },
  /**
   * Insurances component from inside booking overview (BookingDetails) component.
   * @author elemer.zagoni@isdc.ro
   */
  ArrangementItem: {
    onAfterRender: function(msg){
      (function($){
        TA.ArrangementItem.bindValidations(msg);
      })(jQuery);
    },
    bindValidations: function(msg){
      (function($){
        // validations
      })(jQuery);
    },
    submitItem: function(code){
      (function($){
        $('input[id^=sbmArrangementItem-' + code + ']').click();
      })(jQuery);
    }
  },
  ConfirmBooking: {
    print: function(){
      (function($){
        window.print();
        return false;
      })(jQuery);
    }
  }
}; //End of TA
/**
 * Extend the String Class with custom methods.
 * @author elemer.zagoni@isdc.ro
 */
if (!(String.prototype.parseCPDate instanceof Function)) {
  String.prototype.parseCPDate = function(){
    var dateParts = this.split("/");
    var d = null;
    if (dateParts.length < 3) {
      return d;
    }
    try {
      d = new Date(dateParts[2], dateParts[1] - 1, dateParts[0], 0, 0, 0, 0);
    } 
    catch (err) {
    }
    return d;
  }
};
