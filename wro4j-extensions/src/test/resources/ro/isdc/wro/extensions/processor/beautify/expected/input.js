(function(e) {
    e.fn.hoverIntent = function(t, n) {
        var r = {
            sensitivity: 7,
            interval: 100,
            timeout: 0
        };
        r = e.extend(r, n ? {
            over: t,
            out: n
        } : t);
        var i, s, o, u, f = function(e) {
            i = e.pageX, s = e.pageY;
        }, l = function(t, n) {
            n.hoverIntent_t = clearTimeout(n.hoverIntent_t);
            if (Math.abs(o - i) + Math.abs(u - s) < r.sensitivity) return e(n).unbind("mousemove", f), n.hoverIntent_s = 1, r.over.apply(n, [ t ]);
            o = i, u = s, n.hoverIntent_t = setTimeout(function() {
                l(t, n);
            }, r.interval);
        }, c = function(e, t) {
            return t.hoverIntent_t = clearTimeout(t.hoverIntent_t), t.hoverIntent_s = 0, r.out.apply(t, [ e ]);
        }, h = function(e) {
            var t = (e.type == "mouseover" ? e.fromElement : e.toElement) || e.relatedTarget;
            while (t && t != this) try {
                t = t.parentNode;
            } catch (n) {
                t = this;
            }
            if (t == this) return !1;
            var i = jQuery.extend({}, e), s = this;
            s.hoverIntent_t && (s.hoverIntent_t = clearTimeout(s.hoverIntent_t)), e.type == "mouseover" ? (o = i.pageX, u = i.pageY, n(s).bind("mousemove", f), s.hoverIntent_s != 1 && (s.hoverIntent_t = setTimeout(function() {
                l(i, s);
            }, r.interval))) : (n(s).unbind("mousemove", f), s.hoverIntent_s == 1 && (s.hoverIntent_t = setTimeout(function() {
                c(i, s);
            }, r.timeout)));
        };
        return this.mouseover(h).mouseout(h);
    };
})(jQuery);