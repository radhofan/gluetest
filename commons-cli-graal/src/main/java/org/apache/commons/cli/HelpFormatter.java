/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.apache.commons.cli;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.graalvm.polyglot.Value;
/**
 * A formatter of help messages for command line options.
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * Options options = new Options();
 * options.addOption(OptionBuilder.withLongOpt("file").withDescription("The file to be processed").hasArg().withArgName("FILE").isRequired().create('f'));
 * options.addOption(OptionBuilder.withLongOpt("version").withDescription("Print the version of the application").create('v'));
 * options.addOption(OptionBuilder.withLongOpt("help").create('h'));
 *
 * String header = "Do something useful with an input file\n\n";
 * String footer = "\nPlease report issues at http://example.com/issues";
 *
 * HelpFormatter formatter = new HelpFormatter();
 * formatter.printHelp("myapp", header, options, footer, true);
 * </pre>
 *
 * This produces the following output:
 *
 * <pre>
 * usage: myapp -f &lt;FILE&gt; [-h] [-v]
 * Do something useful with an input file
 *
 *  -f,--file &lt;FILE&gt;   The file to be processed
 *  -h,--help
 *  -v,--version       Print the version of the application
 *
 * Please report issues at http://example.com/issues
 * </pre>
 */
public class HelpFormatter {

    /**
     * This class implements the {@code Comparator} interface for comparing Options.
     */
    private static class OptionComparator implements Comparator<Option>, Serializable {
        /** The serial version UID. */
        private static final long serialVersionUID = 5305467873966684014L;

        /**
         * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first argument
         * is less than, equal to, or greater than the second.
         *
         * @param opt1 The first Option to be compared.
         * @param opt2 The second Option to be compared.
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than
         *         the second.
         */
        @Override
        public int compare(final Option opt1, final Option opt2) {
            return opt1.getKey().compareToIgnoreCase(opt2.getKey());
        }
    }

    /**
     * A reference to the Python class
     */
    private static Value clz = ContextInitializer.getPythonClass("help_formatter.py", "HelpFormatter");

    /**
     * A reference to the Python object
     */
    private Value obj;

    /** Default number of characters per line */
    public static final int DEFAULT_WIDTH = 74;

    /** Default padding to the left of each line */
    public static final int DEFAULT_LEFT_PAD = 1;

    /** number of space characters to be prefixed to each description line */
    public static final int DEFAULT_DESC_PAD = 3;

    /** The string to display at the beginning of the usage statement */
    public static final String DEFAULT_SYNTAX_PREFIX = "usage: ";

    /** Default prefix for shortOpts */
    public static final String DEFAULT_OPT_PREFIX = "-";

    /** Default prefix for long Option */
    public static final String DEFAULT_LONG_OPT_PREFIX = "--";

    /**
     * default separator displayed between a long Option and its value
     *
     * @since 1.3
     **/
    public static final String DEFAULT_LONG_OPT_SEPARATOR = " ";

    /** Default name for an argument */
    public static final String DEFAULT_ARG_NAME = "arg";

    /**
     * number of characters per line
     *
     * @deprecated Scope will be made private for next major version - use get/setWidth methods instead.
     */
    @Deprecated
    public int defaultWidth = DEFAULT_WIDTH;

    /**
     * amount of padding to the left of each line
     *
     * @deprecated Scope will be made private for next major version - use get/setLeftPadding methods instead.
     */
    @Deprecated
    public int defaultLeftPad = DEFAULT_LEFT_PAD;

    /**
     * the number of characters of padding to be prefixed to each description line
     *
     * @deprecated Scope will be made private for next major version - use get/setDescPadding methods instead.
     */
    @Deprecated
    public int defaultDescPad = DEFAULT_DESC_PAD;

    /**
     * the string to display at the beginning of the usage statement
     *
     * @deprecated Scope will be made private for next major version - use get/setSyntaxPrefix methods instead.
     */
    @Deprecated
    public String defaultSyntaxPrefix = DEFAULT_SYNTAX_PREFIX;

    /**
     * the new line string
     *
     * @deprecated Scope will be made private for next major version - use get/setNewLine methods instead.
     */
    @Deprecated
    public String defaultNewLine = System.getProperty("line.separator");

    /**
     * the shortOpt prefix
     *
     * @deprecated Scope will be made private for next major version - use get/setOptPrefix methods instead.
     */
    @Deprecated
    public String defaultOptPrefix = DEFAULT_OPT_PREFIX;

    /**
     * the long Opt prefix
     *
     * @deprecated Scope will be made private for next major version - use get/setLongOptPrefix methods instead.
     */
    @Deprecated
    public String defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;

    /**
     * the name of the argument
     *
     * @deprecated Scope will be made private for next major version - use get/setArgName methods instead.
     */
    @Deprecated
    public String defaultArgName = DEFAULT_ARG_NAME;

    /**
     * Comparator used to sort the options when they output in help text
     *
     * Defaults to case-insensitive alphabetical sorting by option key
     */
    protected Comparator<Option> optionComparator = new OptionComparator();


    /**
     * Build a new HelpFormatter and initialize the python object.
     */
    public HelpFormatter() {
        obj = clz.execute();
    }

    /**
     * Return a String of padding of length {@code len}.
     *
     * @param len The length of the String of padding to create.
     *
     * @return The String of padding
     */
    protected String createPadding(final int len) {
        return obj.invokeMember("create_padding", len).asString();
    }

    /**
     * Finds the next text wrap position after {@code startPos} for the text in {@code text} with the column width
     * {@code width}. The wrap point is the last position before startPos+width having a whitespace character (space,
     * \n, \r). If there is no whitespace character before startPos+width, it will return startPos+width.
     *
     * @param text The text being searched for the wrap position
     * @param width width of the wrapped text
     * @param startPos position from which to start the lookup whitespace character
     * @return position on which the text must be wrapped or -1 if the wrap position is at the end of the text
     */
    protected int findWrapPos(final String text, final int width, final int startPos) {
        return obj.invokeMember("find_wrap_pos", text, width, startPos).asInt();
    }

    /**
     * Gets the 'argName'.
     *
     * @return the 'argName'
     */
    public String getArgName() {
        return obj.invokeMember("get_arg_name").asString();
    }

    /**
     * Gets the 'descPadding'.
     *
     * @return the 'descPadding'
     */
    public int getDescPadding() {
        return obj.invokeMember("get_desc_padding").asInt();
    }

    /**
     * Gets the 'leftPadding'.
     *
     * @return the 'leftPadding'
     */
    public int getLeftPadding() {
        return obj.invokeMember("get_left_padding").asInt();
    }

    /**
     * Gets the 'longOptPrefix'.
     *
     * @return the 'longOptPrefix'
     */
    public String getLongOptPrefix() {
        return obj.invokeMember("get_long_opt_prefix").asString();
    }

    /**
     * Gets the separator displayed between a long option and its value.
     *
     * @return the separator
     * @since 1.3
     */
    public String getLongOptSeparator() {
        return obj.invokeMember("get_long_opt_separator").asString();
    }

    /**
     * Gets the 'newLine'.
     *
     * @return the 'newLine'
     */
    public String getNewLine() {
        return obj.invokeMember("get_new_line").asString();
    }

    /**
     * Comparator used to sort the options when they output in help text. Defaults to case-insensitive alphabetical sorting
     * by option key.
     *
     * @return the {@link Comparator} currently in use to sort the options
     * @since 1.2
     */
    public Comparator<Option> getOptionComparator() {
        return this.optionComparator;
    }

    /**
     * Gets the 'optPrefix'.
     *
     * @return the 'optPrefix'
     */
    public String getOptPrefix() {
        return obj.invokeMember("get_opt_prefix").asString();
    }

    /**
     * Gets the 'syntaxPrefix'.
     *
     * @return the 'syntaxPrefix'
     */
    public String getSyntaxPrefix() {
        return obj.invokeMember("get_syntax_prefix").asString();
    }

    /**
     * Gets the 'width'.
     *
     * @return the 'width'
     */
    public int getWidth() {
        return obj.invokeMember("get_width").asInt();
    }

    /**
     * Print the help for {@code options} with the specified command line syntax. This method prints help information
     * to System.out.
     *
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     */
    public void printHelp(final int width, final String cmdLineSyntax, final String header, final Options options, final String footer) {
        printHelp(width, cmdLineSyntax, header, options, footer, false);
    }

    /**
     * Print the help for {@code options} with the specified command line syntax. This method prints help information
     * to System.out.
     *
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated usage statement
     */
    public void printHelp(final int width, final String cmdLineSyntax, final String header, final Options options, final String footer,
        final boolean autoUsage) {
        final PrintWriter pw = new PrintWriter(System.out);

        printHelp(pw, width, cmdLineSyntax, header, options, getLeftPadding(), getDescPadding(), footer, autoUsage);
        pw.flush();
    }

    /**
     * Print the help for {@code options} with the specified command line syntax.
     *
     * @param pw the writer to which the help will be written
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param leftPad the number of characters of padding to be prefixed to each line
     * @param descPad the number of characters of padding to be prefixed to each description line
     * @param footer the banner to display at the end of the help
     *
     * @throws IllegalStateException if there is no room to print a line
     */
    public void printHelp(final PrintWriter pw, final int width, final String cmdLineSyntax, final String header, final Options options, final int leftPad,
        final int descPad, final String footer) {
        printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer, false);
    }

    /**
     * Print the help for {@code options} with the specified command line syntax.
     *
     * @param pw the writer to which the help will be written
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param leftPad the number of characters of padding to be prefixed to each line
     * @param descPad the number of characters of padding to be prefixed to each description line
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated usage statement
     *
     * @throws IllegalStateException if there is no room to print a line
     */
    public void printHelp(final PrintWriter pw, final int width, final String cmdLineSyntax, final String header, final Options options, final int leftPad,
        final int descPad, final String footer, final boolean autoUsage) {
        if (cmdLineSyntax == null || cmdLineSyntax.isEmpty()) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }

        if (autoUsage) {
            printUsage(pw, width, cmdLineSyntax, options);
        } else {
            printUsage(pw, width, cmdLineSyntax);
        }

        if (header != null && !header.isEmpty()) {
            printWrapped(pw, width, header);
        }

        printOptions(pw, width, options, leftPad, descPad);

        if (footer != null && !footer.isEmpty()) {
            printWrapped(pw, width, footer);
        }
    }

    /**
     * Print the help for {@code options} with the specified command line syntax. This method prints help information
     * to System.out.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param options the Options instance
     */
    public void printHelp(final String cmdLineSyntax, final Options options) {
        printHelp(getWidth(), cmdLineSyntax, null, options, null, false);
    }

    /**
     * Print the help for {@code options} with the specified command line syntax. This method prints help information
     * to System.out.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param options the Options instance
     * @param autoUsage whether to print an automatically generated usage statement
     */
    public void printHelp(final String cmdLineSyntax, final Options options, final boolean autoUsage) {
        printHelp(getWidth(), cmdLineSyntax, null, options, null, autoUsage);
    }

    /**
     * Print the help for {@code options} with the specified command line syntax. This method prints help information
     * to System.out.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     */
    public void printHelp(final String cmdLineSyntax, final String header, final Options options, final String footer) {
        printHelp(cmdLineSyntax, header, options, footer, false);
    }

    /**
     * Print the help for {@code options} with the specified command line syntax. This method prints help information
     * to System.out.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the beginning of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated usage statement
     */
    public void printHelp(final String cmdLineSyntax, final String header, final Options options, final String footer, final boolean autoUsage) {
        printHelp(getWidth(), cmdLineSyntax, header, options, footer, autoUsage);
    }

    /**
     * Print the help for the specified Options to the specified writer, using the specified width, left padding and
     * description padding.
     *
     * @param pw The printWriter to write the help to
     * @param width The number of characters to display per line
     * @param options The command line Options
     * @param leftPad the number of characters of padding to be prefixed to each line
     * @param descPad the number of characters of padding to be prefixed to each description line
     */
    public void printOptions(final PrintWriter pw, final int width, final Options options, final int leftPad, final int descPad) {
        final StringBuffer sb = new StringBuffer();

        renderOptions(sb, width, options, leftPad, descPad);
        pw.println(sb.toString());
    }

    /**
     * Print the cmdLineSyntax to the specified writer, using the specified width.
     *
     * @param pw The printWriter to write the help to
     * @param width The number of characters per line for the usage statement.
     * @param cmdLineSyntax The usage statement.
     */
    public void printUsage(final PrintWriter pw, final int width, final String cmdLineSyntax) {
        final int argPos = cmdLineSyntax.indexOf(' ') + 1;

        printWrapped(pw, width, getSyntaxPrefix().length() + argPos, getSyntaxPrefix() + cmdLineSyntax);
    }

    /**
     * Prints the usage statement for the specified application.
     *
     * @param pw The PrintWriter to print the usage statement
     * @param width The number of characters to display per line
     * @param app The application name
     * @param options The command line Options
     */
    public void printUsage(final PrintWriter pw, final int width, final String app, final Options options) {
        // initialize the string buffer
        final StringBuffer buff = new StringBuffer(getSyntaxPrefix()).append(app).append(" ");
        // get Option list and sort within Java
        ArrayList<Option> optList = new ArrayList<Option>(options.getOptions());
        if (getOptionComparator() != null)
            Collections.sort(optList, getOptionComparator());
    
        obj.invokeMember("print_usage", null, width, null, app, options, buff, optList);
        printWrapped(pw, width, buff.toString().indexOf(' ') + 1, buff.toString());
    }

    /**
     * Print the specified text to the specified PrintWriter.
     *
     * @param pw The printWriter to write the help to
     * @param width The number of characters to display per line
     * @param nextLineTabStop The position on the next line for the first tab.
     * @param text The text to be written to the PrintWriter
     */
    public void printWrapped(final PrintWriter pw, final int width, final int nextLineTabStop, final String text) {
        final StringBuffer sb = new StringBuffer(text.length());

        renderWrappedTextBlock(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }

    /**
     * Print the specified text to the specified PrintWriter.
     *
     * @param pw The printWriter to write the help to
     * @param width The number of characters to display per line
     * @param text The text to be written to the PrintWriter
     */
    public void printWrapped(final PrintWriter pw, final int width, final String text) {
        printWrapped(pw, width, 0, text);
    }

    /**
     * Render the specified Options and return the rendered Options in a StringBuffer.
     *
     * @param sb The StringBuffer to place the rendered Options into.
     * @param width The number of characters to display per line
     * @param options The command line Options
     * @param leftPad the number of characters of padding to be prefixed to each line
     * @param descPad the number of characters of padding to be prefixed to each description line
     *
     * @return the StringBuffer with the rendered Options contents.
     */
    protected StringBuffer renderOptions(final StringBuffer sb, final int width, final Options options, final int leftPad, final int descPad) {
        ArrayList<Option> optList = new ArrayList<Option>(options.helpOptions());
        if (getOptionComparator() != null)
            Collections.sort(optList, getOptionComparator());
        obj.invokeMember("render_options", sb, width, options, leftPad, descPad, optList);
        return sb;
    }

    /**
     * Render the specified text and return the rendered Options in a StringBuffer.
     *
     * @param sb The StringBuffer to place the rendered text into.
     * @param width The number of characters to display per line
     * @param nextLineTabStop The position on the next line for the first tab.
     * @param text The text to be rendered.
     *
     * @return the StringBuffer with the rendered Options contents.
     */
    protected StringBuffer renderWrappedText(final StringBuffer sb, final int width, int nextLineTabStop, String text) {
        obj.invokeMember("render_wrapped_text", sb, width, nextLineTabStop, text);
        return sb;
    }

    /**
     * Render the specified text width a maximum width. This method differs from renderWrappedText by not removing leading
     * spaces after a new line.
     *
     * @param sb The StringBuffer to place the rendered text into.
     * @param width The number of characters to display per line
     * @param nextLineTabStop The position on the next line for the first tab.
     * @param text The text to be rendered.
     */
    private Appendable renderWrappedTextBlock(final StringBuffer sb, final int width, final int nextLineTabStop, final String text) {
        obj.invokeMember("_render_wrapped_text_block", sb, width, nextLineTabStop, text);
        return sb;
    }

    /**
     * Remove the trailing whitespace from the specified String.
     *
     * @param s The String to remove the trailing padding from.
     *
     * @return The String of without the trailing padding
     */
    protected String rtrim(final String s) {
        return obj.invokeMember("rtrim", s).asString();
    }

    /**
     * Sets the 'argName'.
     *
     * @param name the new value of 'argName'
     */
    public void setArgName(final String name) {
        obj.invokeMember("set_arg_name", name);
    }

    /**
     * Sets the 'descPadding'.
     *
     * @param padding the new value of 'descPadding'
     */
    public void setDescPadding(final int padding) {
        obj.invokeMember("set_desc_padding", padding);
    }

    /**
     * Sets the 'leftPadding'.
     *
     * @param padding the new value of 'leftPadding'
     */
    public void setLeftPadding(final int padding) {
        obj.invokeMember("set_left_padding", padding);
    }

    /**
     * Sets the 'longOptPrefix'.
     *
     * @param prefix the new value of 'longOptPrefix'
     */
    public void setLongOptPrefix(final String prefix) {
        obj.invokeMember("set_long_opt_prefix", prefix);
    }

    /**
     * Set the separator displayed between a long option and its value. Ensure that the separator specified is supported by
     * the parser used, typically ' ' or '='.
     *
     * @param longOptSeparator the separator, typically ' ' or '='.
     * @since 1.3
     */
    public void setLongOptSeparator(final String longOptSeparator) {
        obj.invokeMember("set_long_opt_separator", longOptSeparator);
    }

    /**
     * Sets the 'newLine'.
     *
     * @param newline the new value of 'newLine'
     */
    public void setNewLine(final String newline) {
        obj.invokeMember("set_new_line", newline);
    }

    /**
     * Set the comparator used to sort the options when they output in help text. Passing in a null comparator will keep the
     * options in the order they were declared.
     *
     * @param comparator the {@link Comparator} to use for sorting the options
     * @since 1.2
     */
    public void setOptionComparator(final Comparator<Option> comparator) {
        this.optionComparator = comparator;
    }

    /**
     * Sets the 'optPrefix'.
     *
     * @param prefix the new value of 'optPrefix'
     */
    public void setOptPrefix(final String prefix) {
        obj.invokeMember("set_opt_prefix", prefix);
    }

    /**
     * Sets the 'syntaxPrefix'.
     *
     * @param prefix the new value of 'syntaxPrefix'
     */
    public void setSyntaxPrefix(final String prefix) {
        obj.invokeMember("set_syntax_prefix", prefix);
    }

    /**
     * Sets the 'width'.
     *
     * @param width the new value of 'width'
     */
    public void setWidth(final int width) {
        obj.invokeMember("set_width", width);
    }

}
