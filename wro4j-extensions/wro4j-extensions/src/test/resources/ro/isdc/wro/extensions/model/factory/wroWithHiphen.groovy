groups {
  "group-ref" {
    css("path/to/style.css")
  }
  "group-with-hiphen" {
    js("/path/to/resource.js")
    groupRef("group-ref")
  }
}