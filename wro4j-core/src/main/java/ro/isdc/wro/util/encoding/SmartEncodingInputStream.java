package ro.isdc.wro.util.encoding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * <code>SmartEncodingInputStream</code> extends an <code>InputStream</code> with a special
 * constructor and a special method for dealing with text files encoded within different charsets.
 * </p>
 * <p>
 * It surrounds a normal <code>InputStream</code> whatever it may be (<code>FileInputStream</code>...). It reads a
 * buffer of a defined length. Then with this byte buffer, it uses the class
 * <code>CharsetToolkit</code> to parse this buffer and guess what the encoding is. All this steps
 * are done within the constructor. At this time, you can call the method <code>getReader()</code> to retrieve a
 * <code>Reader</code> created with the good charset, as guessed while parsing the first bytes of the file. This
 * <code>Reader</code> reads inside the <code>SmartEncodingInputStream</code>. It reads first in
 * the internal buffer, then when we reach the end of the buffer, the underlying InputStream is read with the default
 * read method.
 * </p>
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * FileInputStream fis = new FileInputStream(&quot;utf-8.txt&quot;);
 * SmartEncodingInputStream smartIS = new SmartEncodingInputStream(fis);
 * Reader reader = smartIS.getReader();
 * BufferedReader bufReader = new BufferedReader(reader);
 *
 * String line;
 * while ((line = bufReader.readLine()) != null) {
 *   System.out.println(line);
 * }
 * </pre>
 *
 * Date: 23 juil. 2002
 *
 * @author Guillaume Laforge
 */
public class SmartEncodingInputStream
    extends InputStream {
  private static final Logger LOG = LoggerFactory.getLogger(SmartEncodingInputStream.class);
  private final InputStream is;
  private int bufferLength;
  private final byte[] buffer;
  private int counter;
  private final Charset charset;

  public static final int BUFFER_LENGTH_2KB = 2048;
  public static final int BUFFER_LENGTH_4KB = 4096;
  public static final int BUFFER_LENGTH_8KB = 8192;

  /**
   * <p>
   * Constructor of the <code>SmartEncodingInputStream</code> class. The wider the buffer is, the
   * most sure you are to have guessed the encoding of the <code>InputStream</code> you wished to get a
   * <code>Reader</code> from.
   * </p>
   * <p>
   * It is possible to defined
   * </p>
   *
   * @param is
   *          the <code>InputStream</code> of which we want to create a <code>Reader</code> with the encoding guessed
   *          from the first buffer of the file.
   * @param bufferLength
   *          the length of the buffer that is used to guess the encoding.
   * @param defaultCharset
   *          specifies the default <code>Charset</code> to use when an 8-bit <code>Charset</code> is guessed. This
   *          parameter may be null, in this case the default system charset is used as definied in the system property
   *          "file.encoding" read by the method <code>getDefaultSystemCharset()</code> from the class
   *          <code>CharsetToolkit</code>.
   * @param enforce8Bit
   *          enforce the use of the specified default <code>Charset</code> in case the encoding US-ASCII is recognized.
   * @throws IOException
   */
  public SmartEncodingInputStream(final InputStream is, final int bufferLength, final Charset defaultCharset,
      final boolean enforce8Bit) throws IOException {
    this.is = is;
    this.bufferLength = bufferLength;
    this.buffer = new byte[bufferLength];
    this.counter = 0;

    this.bufferLength = is.read(buffer);
    final CharsetToolkit charsetToolkit = new CharsetToolkit(buffer, defaultCharset);
    charsetToolkit.setEnforce8Bit(enforce8Bit);
    this.charset = charsetToolkit.guessEncoding();
    LOG.debug("detected charset: " + charset);
  }

  /**
   * Constructor of the <code>SmartEncodingInputStream</code>. With this constructor, the default
   * <code>Charset</code> used when an 8-bit encoding is guessed does not need to be specified. The default system
   * charset will be used instead.
   *
   * @param is
   *          is the <code>InputStream</code> of which we want to create a <code>Reader</code> with the encoding guessed
   *          from the first buffer of the file.
   * @param bufferLength
   *          the length of the buffer that is used to guess the encoding.
   * @param defaultCharset
   *          specifies the default <code>Charset</code> to use when an 8-bit <code>Charset</code> is guessed. This
   *          parameter may be null, in this case the default system charset is used as definied in the system property
   *          "file.encoding" read by the method <code>getDefaultSystemCharset()</code> from the class
   *          <code>CharsetToolkit</code>.
   * @throws IOException
   */
  public SmartEncodingInputStream(final InputStream is, final int bufferLength, final Charset defaultCharset)
      throws IOException {
    this(is, bufferLength, defaultCharset, true);
  }

  /**
   * Constructor of the <code>SmartEncodingInputStream</code>. With this constructor, the default
   * <code>Charset</code> used when an 8-bit encoding is guessed does not need to be specified. The default system
   * charset will be used instead.
   *
   * @param is
   *          is the <code>InputStream</code> of which we want to create a <code>Reader</code> with the encoding guessed
   *          from the first buffer of the file.
   * @param bufferLength
   *          the length of the buffer that is used to guess the encoding.
   * @throws IOException
   */
  public SmartEncodingInputStream(final InputStream is, final int bufferLength) throws IOException {
    this(is, bufferLength, null, true);
  }

  /**
   * Constructor of the <code>SmartEncodingInputStream</code>. With this constructor, the default
   * <code>Charset</code> used when an 8-bit encoding is guessed does not need to be specified. The default system
   * charset will be used instead. The buffer length does not need to be specified either. A default buffer length of 4
   * KB is used.
   *
   * @param is
   *          is the <code>InputStream</code> of which we want to create a <code>Reader</code> with the encoding guessed
   *          from the first buffer of the file.
   * @throws IOException
   */
  public SmartEncodingInputStream(final InputStream is) throws IOException {
    this(is, SmartEncodingInputStream.BUFFER_LENGTH_8KB, null, true);
  }

  /**
   * Implements the method <code>read()</code> as defined in the <code>InputStream</code> interface. As a certain number
   * of bytes has already been read from the underlying <code>InputStream</code>, we first read the bytes of this
   * buffer, otherwise, we directly read the rest of the stream from the underlying <code>InputStream</code>.
   *
   * @return the total number of bytes read into the buffer, or <code>-1</code> is there is no more data because the end
   *         of the stream has been reached.
   * @throws IOException
   */
  @Override
  public int read()
      throws IOException {
    if (counter < bufferLength)
      return buffer[counter++];
    else
      return is.read();
  }

  /**
   * Gets a <code>Reader</code> with the right <code>Charset</code> as guessed by reading the beginning of the
   * underlying <code>InputStream</code>.
   *
   * @return a <code>Reader</code> defined with the right encoding.
   */
  public Reader getReader() {
    return new InputStreamReader(this, this.charset);
  }

  /**
   * Retrieves the <code>Charset</code> as guessed from the underlying <code>InputStream</code>.
   *
   * @return the <code>Charset</code> guessed.
   */
  public Charset getEncoding() {
    return this.charset;
  }
}
