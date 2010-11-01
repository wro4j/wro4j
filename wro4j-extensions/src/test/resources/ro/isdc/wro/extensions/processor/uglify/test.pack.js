jQuery.fn.extend({
	data: function( key, value ) {
		if ( typeof key === "undefined" ) {
			return this.length ? jQuery.data( this[0] ) : null;

		} else if ( typeof key === "object" ) {
			return this.each(function() {
				jQuery.data( this, key );
			});
		}

		var parts = key.split(".");
		parts[1] = parts[1] ? "." + parts[1] : "";

		if ( value === undefined ) {
			//var data = this.triggerHandler("getData" + parts[1] + "!", [parts[0]]);

			// Try to fetch any internally stored data first
			if ( data === undefined && this.length ) {
				data = jQuery.data( this[0], key );

				// If nothing was found internally, try to fetch any
				// data from the HTML5 data-* attribute
				if ( data === undefined && this[0].nodeType === 1 ) {
					data = this[0].getAttribute( "data-" + key );

					if ( typeof data === "string" ) {
						try {
							data = data === "true" ? true :
								data === "false" ? false :
								data === "null" ? null :
								!jQuery.isNaN( data ) ? parseFloat( data ) :
								rbrace.test( data ) ? jQuery.parseJSON( data ) :
								data;
						} catch( e ) {}

					} else {
						data = undefined;
					}
				}
			}

			return data === undefined && parts[1] ?
				this.data( parts[0] ) :
				data;

		} else {
			return this.each(function() {
				var $this = jQuery( this ), args = [ parts[0], value ];

				$this.triggerHandler( "setData" + parts[1] + "!", args );
				jQuery.data( this, key, value );
				$this.triggerHandler( "changeData" + parts[1] + "!", args );
			});
		}
	},

	removeData: function( key ) {
		return this.each(function() {
			jQuery.removeData( this, key );
		});
	}
});


