(function() {

})();

// Copyright 2007, 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

var Tapestry = {

    /** Event that allows observers to perform cross-form validation after individual
     *  fields have performed their validation. The form element is passed as the
     *  event memo. Observers may set the validationError property of the Form's Tapestry object to true (which
     *  will prevent form submission).
     */
    FORM_VALIDATE_EVENT : "tapestry:formvalidate",

    /** Event fired just before the form submits, to allow observers to make
     *  final preparations for the submission, such as updating hidden form fields.
     *  The form element is passed as the event memo.
     */
    FORM_PREPARE_FOR_SUBMIT_EVENT : "tapestry:formprepareforsubmit",

    /**
     *  Form event fired after prepare.
     */
    FORM_PROCESS_SUBMIT_EVENT : "tapestry:formprocesssubmit",

    /** Event, triggered on a field element, to cause observers validate the format of the input, and potentially
     *  reformat it. The field will be passed to observers as the event memo.  This event will be followed by
     *  FIELD_VALIDATE_EVENT, if the field's value is non-blank. Observers may invoke Element.showValidationMessage()
     *  to identify that the field is in error (and decorate the field and show a popup error message).
     */
    FIELD_FORMAT_EVENT : "tapestry:fieldformat",

    /** Event, triggered on a field element, to cause observers to validate the input. The field will be passed
     * to observers as the event memo.    Observers may invoke Element.showValidationMessage()
     *  to identify that the field is in error (and decorate the field and show a popup error message).
     */
    FIELD_VALIDATE_EVENT : "tapestry:fieldvalidate",

    /** Event, triggered on the document object, which identifies the current focus element. */
    FOCUS_CHANGE_EVENT : "tapestry:focuschange",

    /** When false, the default, the Tapestry.debug() function will be a no-op. */
    DEBUG_ENABLED : false,

    /** Time, in seconds, that console messages are visible. */
    CONSOLE_DURATION : 60,

    // Adds a callback function that will be invoked when the DOM is loaded (which
    // occurs *before* window.onload, which has to wait for images and such to load
    // first.  This simply observes the dom:loaded event on the document object (support for
    // which is provided by Prototype).
    onDOMLoaded : function(callback)
    {
        document.observe("dom:loaded", callback);
    },

    /** Find all elements marked with the "t-invisible" CSS class and hide()s them, so that
     * Prototype's visible() method operates correctly.                                    In addition,
     * finds form control elements and adds additional listeners to them to support
     * form field input validation.
     *
     * <p>This is invoked when the
     * DOM is first loaded, and AGAIN whenever dynamic content is loaded via the Zone
     * mechanism.
     */
    onDomLoadedCallback : function()
    {
        Tapestry.ScriptManager.initialize();

        $$(".t-invisible").each(function(element)
        {
            element.hide();
            element.removeClassName("t-invisible");
        });

        // Adds a focus observer that fades all error popups except for the
        // field in question.

        $$("INPUT", "SELECT", "TEXTAREA").each(function(element)
        {
            // Due to Ajax, we may execute the callback multiple times,
            // and we don't want to add multiple listeners to the same
            // element.

            var t = $T(element);

            if (! t.observingFocusChange)
            {
                element.observe("focus", function()
                {
                    document.fire(Tapestry.FOCUS_CHANGE_EVENT, element);
                });

                t.observingFocusChange = true;
            }
        });

        // When a submit element is clicked, record the name of the element
        // on the associated form. This is necessary for some Ajax processing
        // TAPESTRY-2324.

        $$("INPUT[type=submit]").each(function(element)
        {
            var t = $T(element);

            if (!t.trackingClicks)
            {
                element.observe("click", function()
                {
                    $T(element.form).lastSubmit = element;
                });

                t.trackingClicks = true;
            }
        });
    },

    /* Generalized initialize function for Tapestry, used to help minimize the amount of JavaScript
     * for the page by removing redundancies such as repeated Object and method names. The spec
     * is a hash whose keys are the names of methods of the Tapestry.Initializer object.
     * The value is an array of arrays.  The outer arrays represent invocations
     * of the method.  The inner array are the parameters for each invocation.
     * As an optimization, the inner value may not be an array but instead
     * a single value.
     */
    init : function(spec)
    {
        $H(spec).each(function(pair)
        {
            var functionName = pair.key;

            var initf = Tapestry.Initializer[functionName];

            if (initf == undefined)
            {
                Tapestry.error("Function Tapestry.Initializer.#{name}() does not exist.", { name:functionName });
                return;
            }

            pair.value.each(function(parameterList)
            {
                if (! Object.isArray(parameterList))
                {
                    parameterList = [parameterList];
                }

                initf.apply(this, parameterList);
            });
        });
    },

    /** Formats and displays an error message on the console. */
    error : function (message, substitutions)
    {
        Tapestry.updateConsole("t-err", message, substitutions);
    },

    /** Formats and displays a warning on the console. */
    warn : function (message, substitutions)
    {
        Tapestry.updateConsole("t-warn", message, substitutions);
    },

    /** Formats and displays a debug message on the console, if Tapestry.DEBUG_ENABLED is true. */
    debug : function (message, substitutions)
    {
        if (Tapestry.DEBUG_ENABLED)
            Tapestry.updateConsole("t-debug", message, substitutions);
    },

    /** Formats a message and updates the console. The console is virtual
     *  when FireBug is not present, the messages float in the upper-left corner
     *  of the page and fade out after a short period.  The background color identifies
     *  the severity of the message (red for error, yellow for warnings, grey for debug).
     *  Messages can be clicked, which removes the immediately.
     *
     * When FireBug is present, the error(), warn() and debug() methods do not invoke
     * this; instead those functions are rewritten to write entries into the FireBug console.
     *
     * @param className to use for the div element in the console
     * @param message message template
     * @param substitutions interpolated into the message (if provided)
     */
    updateConsole : function (className, message, substitutions)
    {
        if (substitutions != undefined)
            message = message.interpolate(substitutions);

        if (Tapestry.console == undefined)
        {
            var body = $$("BODY").first();

            Tapestry.console = new Element("div", { 'class': "t-console" });

            body.insert({ bottom: Tapestry.console });
        }

        var div = new Element("div", { 'class': className }).update(message);

        Tapestry.console.insert({ bottom: div });

        var effect = new Effect.BlindUp(div, { delay: Tapestry.CONSOLE_DURATION,
            afterFinish: function()
            {
                div.remove();
            }});

        div.observe("click", function()
        {
            effect.cancel();
            div.remove();
        });

    },

    /**
     * Passed the JSON content of a Tapestry partial markup response, extracts
     * the script and stylesheet information.  JavaScript libraries and stylesheets are loaded,
     * then any script code is evaluated.  All three keys are optional:
     * <dl>
     * <dt>scripts</dt><dd>Array of strings (URIs of scripts)</dd>
     * <dt>stylesheets</dt><dd>Array of hashes, each hash has key href and optional key media</dd>
     * <dt>script</dt> <dd>JavaScript to be executed once all scripts are loaded</dd></dl>
     */
    processScriptInReply : function(reply)
    {
        Tapestry.ScriptManager.addScripts(reply.scripts, reply.script);

        Tapestry.ScriptManager.addStylesheets(reply.stylesheets);

        Tapestry.onDomLoadedCallback();
    },

    /**
     * Default function for handling Ajax-related failures.
     */
    ajaxFailureHandler : function()
    {
        Tapestry.error("Communication with the server failed.");
    },

    /**
     * Processes a typical Ajax request for a URL invoking the provided handler on success.
     * On failure, error() is invoked to inform the user.
     *
     * @param url of Ajax request
     * @param successHandler to invoke on success
     * @return the Ajax.Request object
     */
    ajaxRequest : function(url, successHandler)
    {
        return new Ajax.Request(url, { onSuccess: successHandler, onFailure: Tapestry.ajaxFailureHandler })
    },

    /**
     * Used to reconstruct a complete URL from a path that is (or may be) relative to window.location.
     * This is used when determining if a JavaScript library or CSS stylesheet has already been loaded.
     * Recognizes complete URLs (which are returned unchanged) and absolute paths (which are prefixed
     * with the window.location protocol and host).  Otherwise the correct path is built.  The
     * path may be prefixed with "./" and "../", which will be resolved correctly.
     *
     * @param path
     * @return complete URL as string
     */
    rebuildURL : function(path)
    {
        if (path.match(/^https?:/))
        {
            return path;
        }

        if (path.startsWith("/"))
        {
            var l = window.location;
            return l.protocol + "//" + l.host + path;
        }

        var rootPath = this.stripToLastSlash(window.location.href);

        while (true)
        {
            if (path.startsWith("../"))
            {
                rootPath = this.stripToLastSlash(rootPath.substr(0, rootPath.length - 1));
                path = path.substring(3);
                continue;
            }

            if (path.startsWith("./"))
            {
                path = path.substr(2);
                continue;
            }

            return rootPath + path;
        }
    },

    stripToLastSlash : function(URL)
    {
        var slashx = URL.lastIndexOf("/");

        return URL.substring(0, slashx + 1);
    }

};

Element.addMethods(
{

    /**
     * Works upward from the element, checking to see if the element is visible. Returns false
     * if it finds an invisible container. Returns true if it makes it as far as a (visible) FORM element.
     *
     * Note that this only applies to the CSS definition of visible; it doesn't check that the element
     * is scolled into view.
     *
     * @param element to search up from
     * @return true if visible (and containers visible), false if it or container are not visible
     */
    isDeepVisible : function(element)
    {
        var current = $(element);

        while (true)
        {
            if (! current.visible()) return false;

            if (current.tagName == "FORM") break;

            current = $(current.parentNode)
        }

        return true;
    }
});

Element.addMethods('FORM',
{
    /**
     * Gets or creates the Tapestry.FormEventManager for the form.
     *
     * @param form form element
     */
    getFormEventManager : function(form)
    {
        form = $(form);
        var t = $T(form);

        var manager = t.formEventManager;

        if (manager == undefined)
        {
            manager = new Tapestry.FormEventManager(form);
            t.formEventManager = manager;
        }

        return manager;
    },

    /**
     * Sends an Ajax request to the Form's action. This encapsulates
     * a few things, such as a default onFailure handler, and working
     * around bugs/features in Prototype concerning how
     * submit buttons are processed.
     *
     * @param form used to define the data to be sent in the request
     * @param options      standard Prototype Ajax Options
     * @return Ajax.Request the Ajax.Request created for the request
     */
    sendAjaxRequest : function (form, options)
    {
        form = $(form);

        // Generally, options should not be null or missing,
        // because otherwise there's no way to provide any callbacks!

        options = Object.clone(options || { });

        // Set a default failure handler if none is provided.

        options.onFailure |= Tapestry.ajaxFailureHandler;

        // Find the elements, skipping over any submit buttons.
        // This works around bugs in Prototype 1.6.0.2.

        var elements = form.getElements().reject(function(e)
        {
            return e.tagName == "INPUT" && e.type == "submit";
        });

        var hash = Form.serializeElements(elements, true);

        var lastSubmit = $T(form).lastSubmit;

        // Put the last submit clicked into the hash, emulating
        // what a normal form submit would do.

        if (lastSubmit && lastSubmit.name)
        {
            hash[lastSubmit.name] = $F(lastSubmit);
        }


        // Copy the parameters in, overwriting field values,
        // because Prototype 1.6.0.2 does not.

        Object.extend(hash, options.parameters);

        options.parameters = hash;

        // We'll just assume that the form has an action; this
        // will always be true in Tapestry.

        var url = form.readAttribute('action');

        // Ajax.Request will convert the hash into a query string and post it.

        return new Ajax.Request(url, options);
    }
});

Element.addMethods(['INPUT', 'SELECT', 'TEXTAREA'],
{
    /**
     * Invoked on a form element (INPUT, SELECT, etc.), gets or creates the
     * Tapestry.FieldEventManager for that field.
     *
     * @param field field element
     */
    getFieldEventManager : function(field)
    {
        field = $(field);
        var t = $T(field);

        var manager = t.fieldEventManager;

        if (manager == undefined)
        {
            manager = new Tapestry.FieldEventManager(field);
            t.fieldEventManager = manager;
        }

        return manager;
    },

    /**
     * Obtains the Tapestry.FieldEventManager and asks it to show
     * the validation message.   Sets the  validationError property of the elements tapestry object to true.
     * @param element
     * @param message to display
     */
    showValidationMessage : function(element, message)
    {
        element = $(element);

        $T(element).validationError = true;
        $T(element.form).validationError = true;

        element.getFieldEventManager().showValidationMessage(message);

        return element;
    },

    /**
     * Removes any validation decorations on the field, and
     * hides the error popup (if any) for the field.
     */
    removeDecorations : function(element)
    {
        $(element).getFieldEventManager().removeDecorations();

        return element;
    },

    /** Utility method to add a validator function as an observer as an event.
     *
     * @param element element to observe events on
     * @param eventName name of event to observe
     * @param validator function passed the field's value
     */
    addValidatorAsObserver : function(element, eventName, validator)
    {
        element.observe(eventName, function(event)
        {
            try
            {
                validator.call(this, $F(element));
            }
            catch (message)
            {
                element.showValidationMessage(message);
            }
        });

        return element;
    },

    /**
     * Adds a standard validator for the element, an observer of
     * Tapestry.FIELD_VALIDATE_EVENT. The validator function will be
     * passed the current field value and should throw an error message if
     * the field's value is not valid.
     * @param element field element to validate
     * @param validator function to be passed the field value
     */
    addValidator : function(element, validator)
    {
        return element.addValidatorAsObserver(Tapestry.FIELD_VALIDATE_EVENT, validator);
    },

    /**
     * Adds a standard validator for the element, an observer of
     * Tapestry.FIELD_FORMAT_EVENT. The validator function will be
     * passed the current field value and should throw an error message if
     * the field's value is not valid.
     * @param element field element to validate
     * @param validator function to be passed the field value
     */
    addFormatValidator : function(element, validator)
    {
        return element.addValidatorAsObserver(Tapestry.FIELD_FORMAT_EVENT, validator);
    }
});

/** Container of functions that may be invoked by the Tapestry.init() function. */
Tapestry.Initializer = {

    ajaxFormLoop : function(spec)
    {
        var rowInjector = $(spec.rowInjector);

        $(spec.addRowTriggers).each(function(triggerId)
        {
            $(triggerId).observe("click", function(event)
            {
                $(rowInjector).trigger();

                Event.stop(event);
            })
        });
    },

    formLoopRemoveLink : function(spec)
    {
        var link = $(spec.link);
        var fragmentId = spec.fragment;

        link.observe("click", function(event)
        {
            Event.stop(event);

            var successHandler = function(transport)
            {
                var container = $(fragmentId);
                var fragment = $T(container).formFragment;

                if (fragment != undefined)
                {
                    fragment.hideAndRemove();
                }
                else
                {
                    var effect = Tapestry.ElementEffect.fade(container);

                    effect.options.afterFinish = function()
                    {
                        container.remove();
                    };
                }
            }

            Tapestry.ajaxRequest(spec.url, successHandler);
        });
    },


    /**
     * Convert a form or link into a trigger of an Ajax update that
     * updates the indicated Zone.
     */
    linkZone : function(element, zoneDiv)
    {
        element = $(element);

        // Update the element with the id of zone div. This may be changed dynamically on the client
        // side.

        $T(element).zone = zoneDiv;

        var successHandler = function(transport)
        {
            var reply = transport.responseJSON;

            // Find the zone id for the element, and from there, the Tapestry.Zone object
            // responsible for the zone.  This is evaluated late so that zone can be dynamically
            // changed on the client.  Sorry about the confusion; there's the zone <div>, and
            // there's the Tapestry.Zone object.    Perhaps should rename Tapestry.Zone
            // to Tapestry.ZoneManager?

            var zoneId = $T(element).zone;
            var zoneObject = $T(zoneId).zone;

            zoneObject.show(reply.content);

            Tapestry.processScriptInReply(reply);
        };

        if (element.tagName == "FORM")
        {
            // Turn normal form submission off.

            element.getFormEventManager().preventSubmission = true;

            // After the form is validated and prepared, this code will
            // process the form submission via an Ajax call.  The original submit event
            // will have been cancelled.

            element.observe(Tapestry.FORM_PROCESS_SUBMIT_EVENT, function()
            {
                element.sendAjaxRequest({ onSuccess : successHandler });
            });

            return;
        }

        // Otherwise, assume it's just an ordinary link.

        element.observe("click", function(event)
        {
            Tapestry.ajaxRequest(element.href, successHandler);

            Event.stop(event);
        });
    },

    validate : function (field, specs)
    {
        field = $(field);

        // Force the creation of the form and field event managers.

        $(field.form).getFormEventManager();
        $(field).getFieldEventManager();

        specs.each(function(spec)
        {
            // spec is a 2 or 3 element array.
            // validator function name, message, optional constraint

            var name = spec[0];
            var message = spec[1];
            var constraint = spec[2];

            var vfunc = Tapestry.Validator[name];

            if (vfunc == undefined)
            {
                Tapestry.error("Function Tapestry.Validator.#{name}() does not exist for field '#{fieldName}'.", {name:name, fieldName:pair.key});
                return;
            }

            // Pass the extend field, the provided message, and the constraint object
            // to the Tapestry.Validator function, so that it can, typically, invoke
            // field.addValidator().

            vfunc.call(this, field, message, constraint);
        });
    },

    zone : function(spec)
    {
        new Tapestry.Zone(spec);
    },

    formFragment : function(spec)
    {
        new Tapestry.FormFragment(spec)
    },

    formInjector : function(spec)
    {
        new Tapestry.FormInjector(spec);
    },

    // Links a FormFragment to a trigger (a radio or a checkbox), such that changing the trigger will hide
    // or show the FormFragment. Care should be taken to render the page with the
    // checkbox and the FormFragment('s visibility) in agreement.

    linkTriggerToFormFragment : function(trigger, element)
    {
        trigger = $(trigger);

        if (trigger.type == "radio")
        {
            $(trigger.form).observe("click", function()
            {
                $T(element).formFragment.setVisible(trigger.checked);
            });

            return;
        }

        // Otherwise, we assume it is a checkbox.  The difference is
        // that we can observe just the single checkbox element,
        // rather than handling clicks anywhere in the form (as with
        // the radio).

        trigger.observe("click", function()
        {
            $T(element).formFragment.setVisible(trigger.checked);
        });

    }
};

// When FireBug is available, rewrite the error(), warn() and debug()
// methods to make use of it.
if (window.console)
{
    var createlog = function (log)
    {
        return function(message, substitutions)
        {
            if (substitutions != undefined)
                message = message.interpolate(substitutions);

            log.call(this, message);
        };
    };

    Tapestry.error = createlog(window.console.error);
    Tapestry.warn = createlog(window.console.warn);
    Tapestry.debug = createlog(window.console.debug);
}

// Collection of field based functions related to validation. Each
// function takes a field, a message and an optional constraint value.
// Some functions are related to Translators and work on the format event,
// other's are from Validators and work on the validate event.

Tapestry.Validator = {

    INT_REGEXP : /^(\+|-)?\d+$/,

    FLOAT_REGEXP : /^(\+|-)?((\.\d+)|(\d+(\.\d*)?))$/,

    required : function(field, message)
    {
        field.addFormatValidator(function(value)
        {
            if (value.strip() == '') throw message;
        });
    },

    /** Validate that the input is a numeric integer. */
    integernumber : function(field, message)
    {
        field.addFormatValidator(function(value)
        {
            if (value != '' && ! value.match(Tapestry.Validator.INT_REGEXP)) throw message;
        });
    },

    decimalnumber : function(field, message)
    {
        field.addFormatValidator(function(value)
        {
            if (value != '' && ! value.match(Tapestry.Validator.FLOAT_REGEXP)) throw message;
        });
    },

    minlength : function(field, message, length)
    {
        field.addValidator(function(value)
        {
            if (value.length < length) throw message;
        });
    },

    maxlength : function(field, message, maxlength)
    {
        field.addValidator(function(value)
        {
            if (value.length > maxlength) throw message;
        });
    },

    min : function(field, message, minValue)
    {
        field.addValidator(function(value)
        {
            if (value < minValue) throw message;
        });
    },

    max : function(field, message, maxValue)
    {
        field.addValidator(function(value)
        {
            if (value > maxValue) throw message;
        });
    },

    regexp : function(field, message, pattern)
    {
        var regexp = new RegExp(pattern);

        field.addValidator(function(value)
        {
            if (! regexp.test(value)) throw message;
        });
    }
};

Tapestry.ErrorPopup = Class.create({

    // If the images associated with the error popup are overridden (by overriding Tapestry's default.css stylesheet),
    // then some of these values may also need to be adjusted.

    BUBBLE_VERT_OFFSET : -34,

    BUBBLE_HORIZONTAL_OFFSET : -20,

    BUBBLE_WIDTH: "auto",

    BUBBLE_HEIGHT: "39px",

    initialize : function(field)
    {
        this.field = $(field);

        this.innerSpan = new Element("span");
        this.outerDiv = $(new Element("div", {
            'id' : this.field.id + ":errorpopup",
            'class' : 't-error-popup' })).update(this.innerSpan).hide();

        var body = $$('BODY').first();

        body.insert({ bottom: this.outerDiv });

        this.outerDiv.absolutize();

        this.outerDiv.observe("click", function(event)
        {
            this.ignoreNextFocus = true;

            this.stopAnimation();

            this.outerDiv.hide();

            this.field.activate();

            Event.stop(event);  // Should be domevent.stop(), but that fails under IE
        }.bindAsEventListener(this));

        this.queue = { position: 'end', scope: this.field.id };

        Event.observe(window, "resize", this.repositionBubble.bind(this));

        document.observe(Tapestry.FOCUS_CHANGE_EVENT, function(event)
        {
            if (this.ignoreNextFocus)
            {
                this.ignoreNextFocus = false;
                return;
            }

            var focused = event.memo;

            if (focused == this.field)
            {
                this.fadeIn();
            }
            else
            {
                this.fadeOut();
            }
        }.bind(this));
    },

    showMessage : function(message)
    {
        this.stopAnimation();

        this.innerSpan.update(message);

        this.hasMessage = true;

        this.fadeIn();
    },

    repositionBubble : function()
    {
        var fieldPos = this.field.cumulativeOffset();

        this.outerDiv.setStyle({
            top: (fieldPos[1] + this.BUBBLE_VERT_OFFSET) + "px",
            left: (fieldPos[0] + this.BUBBLE_HORIZONTAL_OFFSET) + "px",
            width: this.BUBBLE_WIDTH,
            height: this.BUBBLE_HEIGHT });
    },

    fadeIn : function()
    {
        if (! this.hasMessage) return;

        this.repositionBubble();

        if (this.status == "fadeIn") return;

        if (this.outerDiv.visible()) return;

        this.animation = new Effect.Appear(this.outerDiv, { queue: this.queue });

        this.status = "fadeIn";
    },

    stopAnimation : function()
    {
        if (this.animation) this.animation.cancel();

        this.animation = null;
        this.status = null;
    },

    fadeOut : function ()
    {
        // Tapestry.debug("fadeOut: " + this.field.id);

        if (this.status == "fadeOut") return;

        this.animation = new Effect.Fade(this.outerDiv, { queue : this.queue });

        this.status = "fadeOut";
    },

    hide : function()
    {
        this.hasMessage = false;

        this.stopAnimation();

        this.outerDiv.hide();
    }
});

Tapestry.FormEventManager = Class.create({

    initialize : function(form)
    {
        this.form = $(form);

        this.form.onsubmit = this.handleSubmit.bindAsEventListener(this);
    },

    handleSubmit : function(domevent)
    {
        var t = $T(this.form);

        t.validationError = false;

        var firstErrorField = null;

        // Locate elements that have an event manager (and therefore, validations)
        // and let those validations execute, which may result in calls to recordError().


        this.form.getElements().each(function(element)
        {
            var fem = $T(element).fieldEventManager;

            if (fem != undefined)
            {
                // Ask the FEM to validate input for the field, which fires
                // a number of events.
                var error = fem.validateInput();

                if (error && ! firstErrorField)
                {
                    firstErrorField = element;
                }
            }
        });

        // Allow observers to validate the form as a whole.  The FormEvent will be visible
        // as event.memo.  The Form will not be submitted if event.result is set to false (it defaults
        // to true).  Still trying to figure out what should get focus from this
        // kind of event.

        this.form.fire(Tapestry.FORM_VALIDATE_EVENT, this.form);

        if (t.validationError)
        {
            Event.stop(domevent); // Should be domevent.stop(), but that fails under IE

            if (firstErrorField) firstErrorField.activate();

            // Because the submission failed, the last submit property is cleared,
            // since the form may be submitted for some other reason later.

            t.lastSubmit = null;

            return false;
        }

        this.form.fire(Tapestry.FORM_PREPARE_FOR_SUBMIT_EVENT, this.form);

        // This flag can be set to prevent the form from submitting normally.
        // This is used for some Ajax cases where the form submission must
        // run via Ajax.Request.

        if (this.preventSubmission)
        {
            // Prevent the normal submission.

            Event.stop(domevent);

            // Instead ...

            this.form.fire(Tapestry.FORM_PROCESS_SUBMIT_EVENT);

            return false;
        }

        // Validation is OK, not doing Ajax, continue as planned.

        return true;
    }
});

Tapestry.FieldEventManager = Class.create({

    initialize : function(field)
    {
        this.field = $(field);

        var id = this.field.id;
        this.label = $(id + ':label');
        this.icon = $(id + ':icon');

        this.field.observe("blur", function()
        {
            this.validateInput(false);
        }.bindAsEventListener(this));
    },


    /** Removes validation decorations if present. Hides the ErrorPopup,
     *  if it exists.
     */
    removeDecorations : function()
    {
        this.field.removeClassName("t-error");

        if (this.label)
            this.label.removeClassName("t-error");

        if (this.icon)
            this.icon.hide();

        if (this.errorPopup)
            this.errorPopup.hide();
    },


    /**
     * Show a validation error message, which will add decorations to the
     * field and it label, make the icon visible, and raise the
     * field's Tapestry.ErrorPopup to show the message.
     * @param message validation message to display
     */
    showValidationMessage : function(message)
    {
        this.field.addClassName("t-error");

        if (this.label)
            this.label.addClassName("t-error");

        if (this.icon)
        {
            if (! this.icon.visible())
                new Effect.Appear(this.icon);
        }

        if (this.errorPopup == undefined)
            this.errorPopup = new Tapestry.ErrorPopup(this.field);

        this.errorPopup.showMessage(message);
    },

    /**
     * Invoked when a form is submitted, or when leaving a field, to perform
     * field validations. Field validations are skipped for disabled fields.
     * If all validations are succesful, any decorations are removed. If any validation
     * fails, an error popup is raised for the field, to display the validation
     * error message.
     *
     * @return true if the field has a validation error
     */
    validateInput : function()
    {
        if (this.field.disabled) return;

        if (! this.field.isDeepVisible()) return;

        var t = $T(this.field);

        t.validationError = false;

        this.field.fire(Tapestry.FIELD_FORMAT_EVENT, this.field);

        // If Format went ok, perhaps do the other validations.

        if (! t.validationError)
        {
            var value = $F(this.field);

            if (value != '')
                this.field.fire(Tapestry.FIELD_VALIDATE_EVENT, this.field);
        }

        // Lastly, if no validation errors were found, remove the decorations.

        if (! t.validationError)
            this.field.removeDecorations();

        return t.validationError;
    }
});

// Wrappers around Prototype and Scriptaculous effects.
// All the functions of this object should have all-lowercase names.
// The methods all return the Effect object they create.

Tapestry.ElementEffect = {

    show : function(element)
    {
        return new Effect.Appear(element);
    },

    highlight : function(element)
    {
        return new Effect.Highlight(element);
    },

    slidedown : function (element)
    {
        return new Effect.SlideDown(element);
    },

    slideup : function(element)
    {
        return new Effect.SlideUp(element);
    },

    fade : function(element)
    {
        return new Effect.Fade(element);
    }
};


Tapestry.Zone = Class.create({
    // spec are the parameters for the Zone:
    // trigger: required -- name or instance of link.
    // element: required -- name or instance of div element to be shown, hidden and updated
    // show: name of Tapestry.ElementEffect function used to reveal the zone if hidden
    // update: name of Tapestry.ElementEffect function used to highlight the zone after it is updated
    initialize: function(spec)
    {
        if (Object.isString(spec))
            spec = { element: spec }

        this.element = $(spec.element);
        this.showFunc = Tapestry.ElementEffect[spec.show] || Tapestry.ElementEffect.show;
        this.updateFunc = Tapestry.ElementEffect[spec.update] || Tapestry.ElementEffect.highlight;

        // Link the div back to this zone.

        $T(this.element).zone = this;

        // Look inside the Zone element for an element with the CSS class "t-zone-update".
        // If present, then this is the elements whose content will be changed, rather
        // then the entire Zone div.  This allows a Zone div to contain "wrapper" markup
        // (borders and such).  Typically, such a Zone element will initially be invisible.
        // The show and update functions apply to the Zone element, not the update element.

        var updates = this.element.select(".t-zone-update");

        this.updateElement = updates.first() || this.element;
    },

    // Updates the content of the div controlled by this Zone, then
    // invokes the show function (if not visible) or the update function (if visible).

    show: function(content)
    {
        this.updateElement.update(content);

        var func = this.element.visible() ? this.updateFunc : this.showFunc;

        func.call(this, this.element);
    }
});

// A class that managed an element (usually a <div>) that is conditionally visible and
// part of the form when visible.

Tapestry.FormFragment = Class.create({

    initialize: function(spec)
    {
        if (Object.isString(spec))
            spec = { element: spec };

        this.element = $(spec.element);

        $T(this.element).formFragment = this;

        this.hidden = $(spec.element + ":hidden");

        this.showFunc = Tapestry.ElementEffect[spec.show] || Tapestry.ElementEffect.slidedown;
        this.hideFunc = Tapestry.ElementEffect[spec.hide] || Tapestry.ElementEffect.slideup;

        $(this.hidden.form).observe(Tapestry.FORM_PREPARE_FOR_SUBMIT_EVENT, function()
        {
            // On a submission, if the fragment is not visible, then wipe out its
            // form submission data, so that no processing or validation occurs on the server.

            if (! this.element.isDeepVisible())
                this.hidden.value = "";
        }.bind(this));
    },

    hide : function()
    {
        if (this.element.visible())
            this.hideFunc(this.element);
    },

    hideAndRemove : function()
    {
        var effect = this.hideFunc(this.element);

        effect.options.afterFinish = function()
        {
            this.element.remove();
        }.bind(this);
    },

    show : function()
    {
        if (! this.element.visible())
            this.showFunc(this.element);
    },

    toggle : function()
    {
        this.setVisible(! this.element.visible());
    },

    setVisible : function(visible)
    {
        if (visible)
        {
            this.show();
            return;
        }

        this.hide();
    }
});

Tapestry.FormInjector = Class.create({

    initialize: function(spec)
    {
        this.element = $(spec.element);
        this.url = spec.url;
        this.below = spec.below;

        this.showFunc = Tapestry.ElementEffect[spec.show] || Tapestry.ElementEffect.highlight;

        this.element.trigger = function()
        {
            var successHandler = function(transport)
            {
                var reply = transport.responseJSON;

                // Clone the FormInjector element (usually a div)
                // to create the new element, that gets inserted
                // before or after the FormInjector's element.

                var newElement = new Element(this.element.tagName, { 'class' : this.element.className });

                // Insert the new element before or after the existing element.

                var param = { };
                param[this.below ? "after" : "before"] = newElement;

                // Add the new element with the downloaded content.

                this.element.insert(param);

                // Update the empty element with the content from the server

                newElement.update(reply.content);

                newElement.id = reply.elementId;

                // Handle any scripting issues.

                Tapestry.processScriptInReply(reply);

                // Add some animation to reveal it all.

                this.showFunc(newElement);

            }.bind(this);

            Tapestry.ajaxRequest(this.url, successHandler);

            return false;
        }.bind(this);
    }
});

/**
 * Coordinates the execution of JavaScript code blocks (via eval) with the loading
 * of an array of <script> elements.
 */
Tapestry.DependentExecutor = Class.create({

    initialize : function(prereqs, dependent)
    {
        this.dependent = dependent;
        this.loaded = 0;
        this.toload = prereqs.length;

        var executor = this;

        prereqs.each(function (scriptElement)
        {
            if (Prototype.Browser.IE)
            {
                var loaded = false;

                scriptElement.onreadystatechange = function ()
                {
                    // IE may fire either loaded or complete, or perhaps even both.
                    if (! loaded && (this.readyState == 'loaded' || this.readyState == 'complete'))
                    {
                        loaded = true;
                        executor.loadComplete(scriptElement);
                    }
                };
            }
            else
            {
                // Much simpler in FF, Safari, etc.
                scriptElement.onload = executor.loadComplete.bindAsEventListener(executor, scriptElement);
            }
        });
    },

    loadComplete : function(element)
    {
        this.loaded++;

        // Evaluated the dependent script only once all the elements have loaded.

        if (this.loaded == this.toload)
            eval(this.dependent);
    }
});

Tapestry.ScriptManager = {

    initialize : function()
    {

        // Check to see if document.script is supported; if not (for example, FireFox),
        // we can fake it.

        this.emulated = false;

        if (! document.scripts)
        {
            this.emulated = true;

            document.scripts = new Array();

            $$('script').each(function (s)
            {
                document.scripts.push(s);
            });
        }
    },

    /**
     * Checks to see if the given collection (of <script> or <style> elements) contains the given asset URL.
     * @param collection
     * @param prop      property to check ('src' for script, 'href' to style).
     * @param assetURL        complete URL (i.e., with protocol, host and port) to the asset
     */
    contains : function (collection, prop, assetURL)
    {
        return $A(collection).any(function (element)
        {
            var existing = element[prop];

            if (existing.blank()) return false;

            var complete =
                    Prototype.Browser.IE ? Tapestry.rebuildURL(existing) : existing;

            return complete == assetURL;
        });

        return false;
    },

    addScripts: function(scripts, dependent)
    {
        var added = new Array();

        if (scripts)
        {
            var emulated = this.emulated;
            // Looks like IE really needs the new <script> tag to be
            // in the <head>. FF doesn't seem to care.
            // See http://unixpapa.com/js/dyna.html
            var head = $$("head").first();

            scripts.each(function(s)
            {
                var assetURL = Tapestry.rebuildURL(s);

                if (Tapestry.ScriptManager.contains(document.scripts, "src", assetURL)) return; // continue to next script

                var element = new Element('script', { src: assetURL, type: 'text/javascript' });

                head.insert({bottom:element});

                added.push(element);

                if (emulated) document.scripts.push(element);
            });

        }

        if (!dependent) return;

        if (added.length)
        {
            new Tapestry.DependentExecutor(added, dependent);
            return;
        }

        eval(dependent);
    },

    addStylesheets : function(stylesheets)
    {
        if (!stylesheets) return;

        var head = $$('head').first();

        $(stylesheets).each(function(s)
        {
            var assetURL = Tapestry.rebuildURL(s.href);

            if (Tapestry.ScriptManager.contains(document.styleSheets, 'href', assetURL)) return; // continue

            var element = new Element('link', { type: 'text/css', rel: 'stylesheet', href: assetURL });

            // Careful about media types, some browser will break if it ends up as 'null'.

            if (s.media != undefined)
                element.writeAttribute('media', s.media);

            head.insert({bottom: element});

        });
    }
};

/**
 * In the spirit of $(), $T() exists to access the <em>Tapestry object</em> for the element. The Tapestry object
 * is used to store additional values related to the element; it is simply an annoymous object stored as property
 * <code>_tapestry</code> of the element, created the first time it is accessed.
 * <p>This mechanism acts as a namespace, and so helps prevent name
 * conflicts that would occur if properties were stored directly on DOM elements, and makes debugging a bit easier (the Tapestry-specific
 * properties are all in one place!).
 * For the moment,
 * added methods stored directly on the object, and are not prefixed in any way, valuing readability over preventing naming conflicts.
 *
 * @param element an element instance or element id
 * @return object Tapestry object for the element
 */
function $T(element)
{
    var e = $(element);
    var t = e._tapestry;

    if (!t)
    {
        t = { };
        e._tapestry = t;
    }

    return t;
}

Tapestry.onDOMLoaded(Tapestry.onDomLoadedCallback);
