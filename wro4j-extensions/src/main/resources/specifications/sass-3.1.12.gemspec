# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{sass}
  s.version = "3.1.12"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = [%q{Nathan Weizenbaum}, %q{Chris Eppstein}, %q{Hampton Catlin}]
  s.date = %q{2011-12-16}
  s.description = %q{      Sass makes CSS fun again. Sass is an extension of CSS3, adding
      nested rules, variables, mixins, selector inheritance, and more.
      It's translated to well-formatted, standard CSS using the
      command line tool or a web-framework plugin.
}
  s.email = %q{sass-lang@googlegroups.com}
  s.executables = [%q{sass}, %q{sass-convert}, %q{scss}]
  s.files = [%q{bin/sass}, %q{bin/sass-convert}, %q{bin/scss}]
  s.homepage = %q{http://sass-lang.com/}
  s.require_paths = [%q{lib}]
  s.required_ruby_version = Gem::Requirement.new(">= 1.8.7")
  s.rubyforge_project = %q{sass}
  s.rubygems_version = %q{1.8.9}
  s.summary = %q{A powerful but elegant CSS compiler that makes CSS fun again.}

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<yard>, [">= 0.5.3"])
      s.add_development_dependency(%q<maruku>, [">= 0.5.9"])
    else
      s.add_dependency(%q<yard>, [">= 0.5.3"])
      s.add_dependency(%q<maruku>, [">= 0.5.9"])
    end
  else
    s.add_dependency(%q<yard>, [">= 0.5.3"])
    s.add_dependency(%q<maruku>, [">= 0.5.9"])
  end
end
