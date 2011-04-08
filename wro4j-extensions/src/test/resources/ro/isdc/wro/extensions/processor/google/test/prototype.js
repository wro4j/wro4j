  Object.extend(Element.Methods, {
    /** alias of: Element.select
     *  Element.getElementsBySelector(@element, selector) -> [Element...]
    **/
    getElementsBySelector: Element.Methods.select,
  
    /**
     *  Element.childElements(@element) -> [Element...]
     *
     *  Collects all of the element's children and returns them as an array of
     *  [[Element.extended extended]] elements, in document order. The first
     *  entry in the array is the topmost child of `element`, the next is the
     *  child after that, etc.
     *
     *  Like all of Prototype's DOM traversal methods, [[Element.childElements]]
     *  ignores text nodes and returns element nodes only.
     *
     *  ##### Example
     *
     *  Assuming:
     *
     *      language: html
     *      <div id="australopithecus">
     *        Some text in a text node
     *        <div id="homo-erectus">
     *          <div id="homo-neanderthalensis"></div>
     *          <div id="homo-sapiens"></div>
     *        </div>
     *      </div>
     *
     *  Then:
     *
     *      $('australopithecus').childElements();
     *      // -> [div#homo-erectus]
     *
     *      $('homo-erectus').childElements();
     *      // -> [div#homo-neanderthalensis, div#homo-sapiens]
     *
     *      $('homo-sapiens').childElements();
     *      // -> []
    **/
    childElements: Element.Methods.immediateDescendants
  });
  
  Element._attributeTranslations = {
    write: {
      names: {
        className: 'class',
        htmlFor:   'for'
      },
      values: { }
    }
  };
  
  if (Prototype.Browser.Opera) {
    Element.Methods.getStyle = Element.Methods.getStyle.wrap(
      function(proceed, element, style) {
        switch (style) {
          case 'height': case 'width':
            // returns '0px' for hidden elements; we want it to return null
            if (!Element.visible(element)) return null;
  
            // returns the border-box dimensions rather than the content-box
            // dimensions, so we subtract padding and borders from the value
            var dim = parseInt(proceed(element, style), 10);
  
            if (dim !== element['offset' + style.capitalize()])
              return dim + 'px';
  
            var properties;
            if (style === 'height') {
              properties = ['border-top-width', 'padding-top',
               'padding-bottom', 'border-bottom-width'];
            }
            else {
              properties = ['border-left-width', 'padding-left',
               'padding-right', 'border-right-width'];
            }
            return properties.inject(dim, function(memo, property) {
              var val = proceed(element, property);
              return val === null ? memo : memo - parseInt(val, 10);
            }) + 'px';
          default: return proceed(element, style);
        }
      }
    );
  
    Element.Methods.readAttribute = Element.Methods.readAttribute.wrap(
      function(proceed, element, attribute) {
        if (attribute === 'title') return element.title;
        return proceed(element, attribute);
      }
    );
  }
  
  else if (Prototype.Browser.IE) {
    Element.Methods.getStyle = function(element, style) {
      element = $(element);
      style = (style == 'float' || style == 'cssFloat') ? 'styleFloat' : style.camelize();
      var value = element.style[style];
      if (!value && element.currentStyle) value = element.currentStyle[style];
  
      if (style == 'opacity') {
        if (value = (element.getStyle('filter') || '').match(/alpha\(opacity=(.*)\)/))
          if (value[1]) return parseFloat(value[1]) / 100;
        return 1.0;
      }
  
      if (value == 'auto') {
        if ((style == 'width' || style == 'height') && (element.getStyle('display') != 'none'))
          return element['offset' + style.capitalize()] + 'px';
        return null;
      }
      return value;
    };
  
    Element.Methods.setOpacity = function(element, value) {
      function stripAlpha(filter){
        return filter.replace(/alpha\([^\)]*\)/gi,'');
      }
      element = $(element);
      var currentStyle = element.currentStyle;
      if ((currentStyle && !currentStyle.hasLayout) ||
        (!currentStyle && element.style.zoom == 'normal'))
          element.style.zoom = 1;
  
      var filter = element.getStyle('filter'), style = element.style;
      if (value == 1 || value === '') {
        (filter = stripAlpha(filter)) ?
          style.filter = filter : style.removeAttribute('filter');
        return element;
      } else if (value < 0.00001) value = 0;
      style.filter = stripAlpha(filter) +
        'alpha(opacity=' + (value * 100) + ')';
      return element;
    };
  
    Element._attributeTranslations = (function(){
  
      var classProp = 'className', 
          forProp = 'for', 
          el = document.createElement('div');
  
      // try "className" first (IE <8)
      el.setAttribute(classProp, 'x');
  
      if (el.className !== 'x') {
        // try "class" (IE 8)
        el.setAttribute('class', 'x');
        if (el.className === 'x') {
          classProp = 'class';
        }
      }
      el = null;
  
      el = document.createElement('label');
      el.setAttribute(forProp, 'x');
      if (el.htmlFor !== 'x') {
        el.setAttribute('htmlFor', 'x');
        if (el.htmlFor === 'x') {
          forProp = 'htmlFor';
        }
      }
      el = null;
  
      return {
        read: {
          names: {
            'class':      classProp,
            'className':  classProp,
            'for':        forProp,
            'htmlFor':    forProp
          },
          values: {
            _getAttr: function(element, attribute) {
              return element.getAttribute(attribute);
            },
            _getAttr2: function(element, attribute) {
              return element.getAttribute(attribute, 2);
            },
            _getAttrNode: function(element, attribute) {
              var node = element.getAttributeNode(attribute);
              return node ? node.value : "";
            },
            _getEv: (function(){
  
              var el = document.createElement('div'), f;
              el.onclick = Prototype.emptyFunction;
              var value = el.getAttribute('onclick');
  
              // IE<8
              if (String(value).indexOf('{') > -1) {
                // intrinsic event attributes are serialized as `function { ... }`
                f = function(element, attribute) {
                  attribute = element.getAttribute(attribute);
                  if (!attribute) return null;
                  attribute = attribute.toString();
                  attribute = attribute.split('{')[1];
                  attribute = attribute.split('}')[0];
                  return attribute.strip();
                };
              }
              // IE8
              else if (value === '') {
                // only function body is serialized
                f = function(element, attribute) {
                  attribute = element.getAttribute(attribute);
                  if (!attribute) return null;
                  return attribute.strip();
                };
              }
              el = null;
              return f;
            })(),
            _flag: function(element, attribute) {
              return $(element).hasAttribute(attribute) ? attribute : null;
            },
            style: function(element) {
              return element.style.cssText.toLowerCase();
            },
            title: function(element) {
              return element.title;
            }
          }
        }
      }
    })();
  
    Element._attributeTranslations.write = {
      names: Object.extend({
        cellpadding: 'cellPadding',
        cellspacing: 'cellSpacing'
      }, Element._attributeTranslations.read.names),
      values: {
        checked: function(element, value) {
          element.checked = !!value;
        },
  
        style: function(element, value) {
          element.style.cssText = value ? value : '';
        }
      }
    };
  
    Element._attributeTranslations.has = {};
  
    $w('colSpan rowSpan vAlign dateTime accessKey tabIndex ' +
        'encType maxLength readOnly longDesc frameBorder').each(function(attr) {
      Element._attributeTranslations.write.names[attr.toLowerCase()] = attr;
      Element._attributeTranslations.has[attr.toLowerCase()] = attr;
    });
  
    (function(v) {
      Object.extend(v, {
        href:        v._getAttr2,
        src:         v._getAttr2,
        type:        v._getAttr,
        action:      v._getAttrNode,
        disabled:    v._flag,
        checked:     v._flag,
        readonly:    v._flag,
        multiple:    v._flag,
        onload:      v._getEv,
        onunload:    v._getEv,
        onclick:     v._getEv,
        ondblclick:  v._getEv,
        onmousedown: v._getEv,
        onmouseup:   v._getEv,
        onmouseover: v._getEv,
        onmousemove: v._getEv,
        onmouseout:  v._getEv,
        onfocus:     v._getEv,
        onblur:      v._getEv,
        onkeypress:  v._getEv,
        onkeydown:   v._getEv,
        onkeyup:     v._getEv,
        onsubmit:    v._getEv,
        onreset:     v._getEv,
        onselect:    v._getEv,
        onchange:    v._getEv
      });
    })(Element._attributeTranslations.read.values);
  
    // We optimize Element#down for IE so that it does not call
    // Element#descendants (and therefore extend all nodes).
    if (Prototype.BrowserFeatures.ElementExtensions) {
      (function() {
        function _descendants(element) {
          var nodes = element.getElementsByTagName('*'), results = [];
          for (var i = 0, node; node = nodes[i]; i++)
            if (node.tagName !== "!") // Filter out comment nodes.
              results.push(node);
          return results;
        }
  
        Element.Methods.down = function(element, expression, index) {
          element = $(element);
          if (arguments.length == 1) return element.firstDescendant();
          return Object.isNumber(expression) ? _descendants(element)[expression] :
            Element.select(element, expression)[index || 0];
        }
      })();
    }
  
  }
  
  else if (Prototype.Browser.Gecko && /rv:1\.8\.0/.test(navigator.userAgent)) {
    Element.Methods.setOpacity = function(element, value) {
      element = $(element);
      element.style.opacity = (value == 1) ? 0.999999 :
        (value === '') ? '' : (value < 0.00001) ? 0 : value;
      return element;
    };
  }
  
  else if (Prototype.Browser.WebKit) {
    Element.Methods.setOpacity = function(element, value) {
      element = $(element);
      element.style.opacity = (value == 1 || value === '') ? '' :
        (value < 0.00001) ? 0 : value;
  
      if (value == 1)
        if (element.tagName.toUpperCase() == 'IMG' && element.width) {
          element.width++; element.width--;
        } else try {
          var n = document.createTextNode(' ');
          element.appendChild(n);
          element.removeChild(n);
        } catch (e) { }
  
      return element;
    };
  }
  
  if ('outerHTML' in document.documentElement) {
    Element.Methods.replace = function(element, content) {
      element = $(element);
  
      if (content && content.toElement) content = content.toElement();
      if (Object.isElement(content)) {
        element.parentNode.replaceChild(content, element);
        return element;
      }
  
      content = Object.toHTML(content);
      var parent = element.parentNode, tagName = parent.tagName.toUpperCase();
  
      if (Element._insertionTranslations.tags[tagName]) {
        var nextSibling = element.next(),
            fragments = Element._getContentFromAnonymousElement(tagName, content.stripScripts());
        parent.removeChild(element);
        if (nextSibling)
          fragments.each(function(node) { parent.insertBefore(node, nextSibling) });
        else
          fragments.each(function(node) { parent.appendChild(node) });
      }
      else element.outerHTML = content.stripScripts();
  
      content.evalScripts.bind(content).defer();
      return element;
    };
  }
  
  Element._returnOffset = function(l, t) {
    var result = [l, t];
    result.left = l;
    result.top = t;
    return result;
  };
  
  Element._getContentFromAnonymousElement = function(tagName, html, force) {
    var div = new Element('div'), 
        t = Element._insertionTranslations.tags[tagName];
    
    var workaround = false;
    if (t) workaround = true;
    else if (force) {
      workaround = true;
      t = ['', '', 0];
    }
        
    if (workaround) {
      // Adding a text node to the beginning of the string (then removing it)
      // fixes an issue in Internet Explorer. See Element#update above.
      div.innerHTML = '&nbsp;' + t[0] + html + t[1];
      div.removeChild(div.firstChild);
      for (var i = t[2]; i--; ) {
        div = div.firstChild;
      }
    }
    else {
      div.innerHTML = html;
    }
    return $A(div.childNodes);
  };
  
  Element._insertionTranslations = {
    before: function(element, node) {
      element.parentNode.insertBefore(node, element);
    },
    top: function(element, node) {
      element.insertBefore(node, element.firstChild);
    },
    bottom: function(element, node) {
      element.appendChild(node);
    },
    after: function(element, node) {
      element.parentNode.insertBefore(node, element.nextSibling);
    },
    tags: {
      TABLE:  ['<table>',                '</table>',                   1],
      TBODY:  ['<table><tbody>',         '</tbody></table>',           2],
      TR:     ['<table><tbody><tr>',     '</tr></tbody></table>',      3],
      TD:     ['<table><tbody><tr><td>', '</td></tr></tbody></table>', 4],
      SELECT: ['<select>',               '</select>',                  1]
    }
  };
  
  (function() {
    var tags = Element._insertionTranslations.tags;
    Object.extend(tags, {
      THEAD: tags.TBODY,
      TFOOT: tags.TBODY,
      TH:    tags.TD
    });
  })();
  
  Element.Methods.Simulated = {
    /**
     *  Element.hasAttribute(@element, attribute) -> Boolean
     *  
     *  Simulates the standard compliant DOM method
     *  [`hasAttribute`](http://www.w3.org/TR/DOM-Level-2-Core/core.html#ID-ElHasAttr)
     *  for browsers missing it (Internet Explorer 6 and 7).
     *  
     *  ##### Example
     *  
     *      language: html
     *      <a id="link" href="http://prototypejs.org">Prototype</a>
     *
     *  Then:
     *
     *      $('link').hasAttribute('href');
     *      // -> true
    **/  
    hasAttribute: function(element, attribute) {
      attribute = Element._attributeTranslations.has[attribute] || attribute;
      var node = $(element).getAttributeNode(attribute);
      return !!(node && node.specified);
    }
  };
  
  Element.Methods.ByTag = { };
  
  Object.extend(Element, Element.Methods);
  
