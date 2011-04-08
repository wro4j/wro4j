if ( left.substr( left.length - 1 ) !== "\\" ) {
	match[1] = (match[1] || "").replace(/\\/g, "");
	set = Expr.find[ type ]( match, context, isXML );
	if ( set != null ) {
		expr = expr.replace( Expr.match[ type ], "" );
	}
}
