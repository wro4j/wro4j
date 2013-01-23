/*
* Copyright 2011 Wro4J
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ro.isdc.wro.extensions.model.factory

import org.junit.Test
import ro.isdc.wro.model.WroModel
import ro.isdc.wro.model.resource.ResourceType
import ro.isdc.wro.WroRuntimeException

/**
 * Test Wro Groovy DSL
 *
 * @author Filirom1
 * @created 19 Jul 2011
 */
class TestGroovyModelParser {

  @Test
  public void testResourceDelegate() {
    //setup:
    def resourceDelegate = new ResourceDelegate()

    //when:
    resourceDelegate.js("js/*.js")
    resourceDelegate.css(minimize: false, "css/*.css")
    resourceDelegate.css(minimize: true, "css/style.css")

    //then:
    assert 3 == resourceDelegate.resources.size()

    assert resourceDelegate.resources[0].minimize
    assert !resourceDelegate.resources[1].minimize
    assert resourceDelegate.resources[2].minimize

    assert ResourceType.JS == resourceDelegate.resources[0].type
    assert ResourceType.CSS == resourceDelegate.resources[1].type
    assert ResourceType.CSS == resourceDelegate.resources[2].type

    assert "js/*.js" == resourceDelegate.resources[0].uri
    assert "css/*.css" == resourceDelegate.resources[1].uri
    assert "css/style.css" == resourceDelegate.resources[2].uri
  }

  @Test
  public void testGroupDelegate() {
    //setup:
    def groupDelegate = new GroupDelegate()

    //when:
    groupDelegate.g1 {
      js("/js/script.js")
      css("/css/style.css")
    }
    groupDelegate.g2 {
      js("/js/script2.js")
    }

    //then:
    def groups = groupDelegate.resolveGroupResources()
    assert 2 == groups.size()
    assert "g1" == groups[0].name
    assert 2 == groups[0].resources.size()
    assert "g2" == groups[1].name
    assert 1 == groups[1].resources.size()
  }

  @Test
  public void testGroupWithGroupRef() {
    //setup:
    def groupDelegate = new GroupDelegate()

    //when:
    groupDelegate.g1 {
      js("/js/script.js")
      css("/css/style.css")
    }
    groupDelegate.g2 {
      groupRef('g1')
      js("/js/script2.js")
    }

    //then:
    def groups = groupDelegate.resolveGroupResources()
    assert 2 == groups.size()
    assert "g2" == groups[1].name
    assert 3 == groups[1].resources.size()
  }

  @Test
  public void testMultipleGroupWithGroupRef() {
    //setup:
    def groupDelegate = new GroupDelegate()

    //when:
    groupDelegate.g1 {
      js("/js/script.js")
      css("/css/style.css")
    }
    groupDelegate.g2 {
      groupRef('g1')
      js("/js/script2.js")
    }
    groupDelegate.g3 {
      groupRef('g2')
      js("/js/script3.js")
    }

    //then:
    def groups = groupDelegate.resolveGroupResources()
    assert 3 == groups.size()
    assert "g3" == groups[2].name
    assert 4 == groups[2].resources.size()
  }

  @Test
  public void testGroupWithGroupRefWithoutOrder() {
    //setup:
    def groupDelegate = new GroupDelegate()

    //when:
    groupDelegate.g1 {
      groupRef('g2')
      js("/js/script.js")
      css("/css/style.css")
    }

    groupDelegate.g2 {
      js("/js/script2.js")
    }

    //then:
    def groups = groupDelegate.resolveGroupResources()
    assert 2 == groups.size()
    assert "g1" == groups[0].name
    assert 3 == groups[0].resources.size()
    assert "g2" == groups[1].name
    assert 1 == groups[1].resources.size()
  }

  @Test
  public void testGroupWithGroupRefImplicit() {
    //setup:
    def groupDelegate = new GroupDelegate()

    //when:
    groupDelegate.g1 {
      js("/js/script.js")
      css("/css/style.css")
    }
    groupDelegate.g2 {
      g1()
      js("/js/script2.js")
    }

    //then:
    def groups = groupDelegate.resolveGroupResources()
    assert 2 == groups.size()
    assert "g2" == groups[1].name
    assert 3 == groups[1].resources.size()
  }

  @Test(expected = WroRuntimeException.class)
  public void testGroupWithUnknownGroupRef() {
    //setup:
    def groupDelegate = new GroupDelegate()

    //when:
    groupDelegate.g1 {
      js("/js/script.js")
      css("/css/style.css")
    }
    groupDelegate.g2 {
      groupRef('unknwon')
      js("/js/script2.js")
    }
    groupDelegate.resolveGroupResources()

  }

  @Test
  public void testWroModelDelegate() {
    //setup:
    def wroModelDelegate = new WroModelDelegate()

    //when:
    wroModelDelegate.groups {
      g1 {
        g2()
        js("/js/script.js")
        css("/css/style.css")
      }
      g2 {
        js("/js/script2.js")
      }
    }

    //then:
    assert 2 == wroModelDelegate.wroModel.groups.size()
    assert 3 == wroModelDelegate.wroModel.groups.find {it.name == "g1"}.resources.size()
    assert 1 == wroModelDelegate.wroModel.groups.find {it.name == "g2"}.resources.size()
  }

  @Test
  public void testParse() {
    //setup:
    def dsl = """
    groups {
      g1 {
        js("/js/script.js")
        css("/css/style.css")
      }
      g2 {
        js("/js/script2.js")
      }
    }
    """

    //when:
    WroModel wroModel = GroovyModelParser.parse(dsl)

    //then:
    assert ["g1", "g2"] == wroModel.groupNames
  }

    @Test
    public void testAbstractGroup() {
        //setup:
        def groupDelegate = new GroupDelegate()

        //when:
        groupDelegate.g1(abstract: true) {
            js("/js1")
        }

        groupDelegate.g2 {
            groupRef('g1')
            js("/js2")
        }

        //then:
        def groups = groupDelegate.resolveGroupResources()
        assert 1 == groups.size()
        assert "g2" == groups[0].name
        assert 2 == groups[0].resources.size()
        assert "/js1" == groups[0].resources[0].uri;
        assert "/js2" == groups[0].resources[1].uri;

    }
}
