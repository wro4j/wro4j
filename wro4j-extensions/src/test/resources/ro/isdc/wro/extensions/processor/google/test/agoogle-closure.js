goog.require( 'goog.dom' ); // compiler complains here..
goog.require( 'goog.events' ); // ... and here

goog.dom.createDom( 'header' );
goog.dom.appendChild( document.body, element );