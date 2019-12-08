package tests.markdown

import mdoc.internal.cli.Settings
import scala.meta.inputs.Input
import scala.meta.io.RelativePath
import scala.meta.testkit.DiffAssertions
import mdoc.internal.markdown.DocumentLinks
import mdoc.internal.markdown.GitHubIdGenerator
import org.scalatest.funsuite.AnyFunSuite

class DocumentLinksSuite extends AnyFunSuite with DiffAssertions {

  def check(name: String, original: String, fn: DocumentLinks => Unit): Unit = {
    test(name) {
      val filename = name + ".md"
      val input = Input.VirtualFile(filename, original)
      val links = DocumentLinks.fromMarkdown(GitHubIdGenerator, RelativePath(filename), input)
      fn(links)
    }
  }

  def references(name: String, original: String, expected: String): Unit = {
    check(name, original, { links =>
      val obtained = links.references.map(_.url).mkString("\n")
      assertNoDiffOrPrintExpected(obtained, expected)
    })
  }

  def definitions(name: String, original: String, expected: String): Unit = {
    check(name, original, { links =>
      val obtained = links.definitions.mkString("\n")
      assertNoDiffOrPrintExpected(obtained, expected)
    })
  }

  definitions(
    "basic",
    """
      |# Title
      |## Subtitle
      |## Two words
    """.stripMargin,
    """
      |title
      |subtitle
      |two-words
    """.stripMargin
  )

  definitions(
    "custom",
    """
      |<a name="name"></a>
      |# Header
      |<a id="id"></a>
    """.stripMargin,
    """
      |name
      |header
      |id
    """.stripMargin
  )

  definitions(
    "dash",
    """
      |# Project - cool
    """.stripMargin,
    """
      |project---cool
    """.stripMargin
  )

  references(
    "link",
    """
      |Link to [my section](#section)
    """.stripMargin,
    """
      |#section
    """.stripMargin
  )

  references(
    "url",
    """
      |Link to [a URL](https://geirsson.com)
    """.stripMargin,
    """
      |https://geirsson.com
    """.stripMargin
  )

}
