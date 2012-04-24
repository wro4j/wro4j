// @author  Andrea Giammarchi
(function(cache) {
	window.JsonHPack = {};
	/**
	 * JSON.hpack(homogeneousCollection:Array[, compression:Number]):Array
	 * 
	 * @param Array
	 *            mono dimensional homogeneous collection of objects to pack
	 * @param [Number]
	 *            optional compression level from 0 to 4 - default 0
	 * @return Array optimized collection
	 */
	JsonHPack.hpack = function(collection, compression) {
		if (3 < compression) { // try evey compression level and returns the
								// best option
			var i = JsonHPack.hbest(collection), result = cache[i];
			cache = [];
		} else { // compress via specified level (default 0)
			var indexOf = Array.prototype.indexOf || function(v) {
				for ( var l = this.length, i = 0; i < l; ++i)
					if (this[i] === v)
						return i;
				return -1
			}, header = [], result = [header], first = collection[0], index = 0, k = 0, len;
			// create list of property names
			for ( var key in first)
				header[index++] = key;
			len = index;
			index = 0;
			// replace objects using arrays respecting header indexes order
			for ( var length = collection.length, i = 0; i < length; ++i) {
				for ( var item = collection[i], row = [], j = 0; j < len; ++j)
					row[j] = item[header[j]];
				result[++index] = row;
			};
			++index;
			// compression 1, 2 or 3
			if (0 < compression) {
				// create a fixed enum type for each property (except numbers)
				for (row = result[1], j = 0; j < len; ++j) {
					if (typeof row[j] != "number") {
						header[j] = [header[j], first = []];
						first.indexOf = indexOf;
						// replace property values with enum index (create entry
						// in enum list if not present)
						for (i = 1; i < index; ++i) {
							var value = result[i][j], l = first.indexOf(value);
							result[i][j] = l < 0 ? first.push(value) - 1 : l;
						};
					};
				};
			};
			// compression 3 only
			if (2 < compression) {
				// Second Attemp:
				// This compression is quite expensive.
				// It calculates the length of all indexes plus the lenght
				// of the enum against the length of values rather than indexes
				// and without enum for each column
				// In this way the manipulation will be hibryd but hopefully
				// worthy in certain situation.
				// not truly suitable for old client CPUs cause it could cost
				// too much
				for (j = 0; j < len; ++j) {
					if (header[j] instanceof Array) {
						for (row = header[j][1], value = [], first = [], k = 0, i = 1; i < index; ++i) {
							value[k] = row[first[k] = result[i][j]];
							++k;
						};
						if (JSON.stringify(value).length < JSON.stringify(first
								.concat(row)).length) {
							for (k = 0, i = 1; i < index; ++i) {
								result[i][j] = value[k];
								++k;
							};
							header[j] = header[j][0];
						};
					};
				};
			}
			// compression 2 only
			else if (1 < compression) {
				// compare the lenght of the entire collection with the length
				// of the enum, if present
				length -= Math.floor(length / 2);
				for (j = 0; j < len; ++j) {
					if (header[j] instanceof Array) {
						// if the collection length - (collection lenght / 2) is
						// lower than enum length
						// maybe it does not make sense to create extra
						// characters in the string for each
						// index representation
						if (length < (first = header[j][1]).length) {
							for (i = 1; i < index; ++i) {
								var value = result[i][j];
								result[i][j] = first[value];
							};
							header[j] = header[j][0];
						};
					};
				};
			};
			// if compression is at least greater than 0
			if (0 < compression) {
				// flat the header Array to remove useless brackets
				for (j = 0; j < len; ++j) {
					if (header[j] instanceof Array) {
						header.splice(j, 1, header[j][0], header[j][1]);
						++len;
						++j;
					};
				};
			};
		};
		return result;
	};

	/**
	 * JSON.hunpack(packedCollection:Array):Array
	 * 
	 * @param Array
	 *            optimized collection to unpack
	 * @return Array original mono dimensional homogeneous collection of objects
	 */
	JsonHPack.hunpack = function(collection) {
		// compatible with every hpack compressed array
		// simply swaps arrays with key/values objects
		for ( var result = [], keys = [], header = collection[0], len = header.length, length = collection.length, index = -1, k = -1, i = 0, l = 0, j, row; i < len; ++i) {
			// list of keys
			keys[++k] = header[i];
			// if adjacent value is an array (enum)
			if (typeof header[i + 1] == "object") {
				++i;
				// replace indexes in the column
				// using enum as collection
				for (j = 1; j < length; ++j) {
					row = collection[j];
					row[l] = header[i][row[l]];
				};
			};
			++l;
		};
		for (i = 0, len = keys.length; i < len; ++i)
			// replace keys with assignment operation ( test becomes
			// o["test"]=a[index]; )
			// make properties safe replacing " char
			keys[i] = 'o["'.concat(keys[i].replace('"', "\\x22"), '"]=a[', i,
					'];');
		// one shot anonymous function with "precompiled replacements"
		var anonymous = Function("o,a", keys.join("") + "return o;");
		for (j = 1; j < length; ++j)
			// replace each item with runtime key/value pairs object
			result[++index] = anonymous({}, collection[j]);
		return result;
	};

	/**
	 * JSON.hclone(packedCollection:Array):Array
	 * 
	 * @param Array
	 *            optimized collection to clone
	 * @return Array a clone of the original collection
	 */
	JsonHPack.hclone = function(collection) {
		// avoid array modifications
		// it could be useful but not that frequent in "real life cases"
		for ( var clone = [], i = 0, length = collection.length; i < length; ++i)
			clone[i] = collection[i].slice(0);
		return clone;
	};

	/**
	 * JSON.hbest(packedCollection:Array):Number
	 * 
	 * @param Array
	 *            optimized collection to clone
	 * @return Number best compression option
	 */
	JsonHPack.hbest = function(collection) {
		// for each compression level [0-4] ...
		for ( var i = 0, j = 0, len = 0, length = 0; i < 4; ++i) {
			// cache result
			cache[i] = JsonHPack.hpack(collection, i);
			// retrieve the JSON length
			len = JSON.stringify(cache[i]).length;
			if (length === 0)
				length = len;
			// choose which one is more convenient
			else if (len < length) {
				length = len;
				j = i;
			};
		};
		// return most convenient convertion
		// please note that with small amount of data
		// native JSON convertion could be smaller
		// [{"k":0}] ==> [["k"],[0]] (9 chars against 11)
		// above example is not real life example and as soon
		// as the list will have more than an object
		// hpack will start to make the difference:
		// [{"k":0},{"k":0}] ==> [["k"],[0],[0]] (17 chars against 15)
		return j;
	};
})([]);
