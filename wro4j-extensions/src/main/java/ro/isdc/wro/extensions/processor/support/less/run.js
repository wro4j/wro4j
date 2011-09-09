var lessIt = function(css) {
    var result;
    var parser = new window.less.Parser();

    parser.parse(css, function (e, root) {
        result = root.toCSS();
    });
    return result;
};
