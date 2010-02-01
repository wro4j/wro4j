var DWR = {
	findOffice : function() {
		var categoryId = $('#searchWithinCombo').val();
		var office = $('#searchOfficeText').val();
		Ajax.findOffice(categoryId,office, function(result){
			//alert(/*JSONstring.make(result)*/result);
			var tBody=$('table.office-overview tbody').html(result);
		});
	}
};