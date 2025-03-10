/*
 * Copyright 2016-2023 The GrooCSS authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.groocss

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.codehaus.groovy.control.CompilerConfiguration

import groovy.transform.*
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.groocss.ext.NumberExtension
import org.groocss.ext.StringExtension

import javax.imageio.ImageIO

/**
 * Entrance to DSL for converting code into CSS.
 */
@CompileStatic
class GrooCSS extends Script implements CurrentKeyFrameHolder {

    static final ThreadLocal<GrooCSS> threadLocalInstance = new ThreadLocal<>()

    /**
     * Converts from given filename containing GrooCSS DSL to given out filename as CSS.
     * @param conf Optional Config object for configuration.
     * @param inName In filename.
     * @param outName Out filename of resulting CSS.
     */
    static void convertFile(Config conf = new Config(), String inName, String outName) {
        convert conf, new File(inName), new File(outName)
    }

    /**
     * Converts from given file containing GrooCSS DSL to given out file as CSS.
     * @param conf Optional Config object for configuration.
     * @param inf Input file with GrooCSS code.
     * @param out Output file of resulting CSS.
     */
    static void convertFile(Config conf = new Config(), File inf, File out) {
        convert conf, inf, out
    }

    /**
     * Converts from given file containing GrooCSS DSL to given out file as CSS.
     * @param conf Optional Config object for configuration.
     * @param inf Input file with GrooCSS code.
     * @param out Output file of resulting CSS.
     * @param charset Charset of file to read (UTF-8 by default).
     * @param addMeta Tells GrooCSS to augment String and Integer classes using metaClass (true by default).
     */
    static void convert(Config conf = new Config(), File inf, File out,
                        String charset = "UTF-8", boolean addMeta = true) {
        convert(conf, inf.newInputStream(), out.newOutputStream(), charset, addMeta)
    }

    /**
     * Converts given file which must explicitly create a GrooCSS instance using {@link #process(Closure)} for example.
     *
     * @param inf Input file containing a GrooCSS.process{} block of code or ''.process{} or similar.
     * @param out Output file of resulting CSS.
     * @param charset1 Charset to use (UTF-8 by default).
     * @param variables Variables to make available to GrooCSS code.
     */
    static void convertWithoutBase(File inf, File out, String charset1 = "UTF-8",
                                   Map<String, Object> variables = null) {
        convertWithoutBase(inf.newInputStream(), out.newOutputStream(), charset1, variables)
    }

    /** Processes a given groocss string and outputs as CSS string.
     * @param conf Optional Config object for configuration.
     * @param groocss Input file with GrooCSS code.
     * @param charset1 Charset to use (UTF-8 by default).
     * @param addMeta Tells GrooCSS to augment String and Integer classes using metaClass (true by default).
     */
    static String convert(Config conf = new Config(), String groocss, String charset1 = "UTF-8", boolean addMeta=true) {
        def out = new ByteArrayOutputStream()
        convert conf, new ByteArrayInputStream(groocss.getBytes(charset1)), out, charset1, addMeta
        out.toString()
    }

    protected static GroovyShell makeShell(boolean addBase = true, Config config1 = null) {
        def binding = new Binding()
        def compilerConfig = new CompilerConfiguration()
        def imports = new ImportCustomizer()
        def packg = 'org.groocss'
        imports.addStarImports(packg)
        compilerConfig.addCompilationCustomizers(imports)
        if (addBase) compilerConfig.scriptBaseClass = "${packg}.GrooCSS"
        if (config1?.variables) config1.variables.each { key, value -> binding.setVariable(key, value) }

        new GroovyShell(GrooCSS.class.classLoader, binding, compilerConfig)
    }

    /** Processes a given InputStream and outputs to given OutputStream.
     * @param conf Optional Config object for configuration.
     * @param inf Input file stream with GrooCSS code.
     * @param out Output file stream of resulting CSS.
     * @param charset1 Charset of file to read (UTF-8 by default).
     * @param addMeta Tells GrooCSS to augment String and Integer classes using metaClass (true by default).
     * */
    static void convert(Config conf = new Config(), InputStream inf, OutputStream out, String charset1 = "UTF-8",
                        boolean addMeta = true) {
        out.withPrintWriter { pw ->
            convert conf, new InputStreamReader(inf, charset1), pw, addMeta
        }
    }

    /** Processes a given Reader and outputs to given PrintWriter.
     * @param conf Optional Config object for configuration.
     * @param reader Input file with GrooCSS code.
     * @param writer Output file of resulting CSS.
     * @param addMeta Tells GrooCSS to augment String and Integer classes using metaClass (true by default).
     */
    static void convert(Config conf = new Config(), Reader reader, PrintWriter writer, boolean addMeta = true) {

        final GrooCSS previousGrooCssInstance = threadLocalInstance.get()

        reader.withCloseable { input ->
            def shell = makeShell(true, conf)
            def script = shell.parse(input)
            if (addMeta) script.invokeMethod('initMetaClasses', true)
            script.invokeMethod('setConfig', conf)
            MediaCSS css = runAndProcessScript(script)
            writer.withCloseable { pw ->
                css.writeTo pw
                pw.flush()
            }
            resetThreadLocalInstance(previousGrooCssInstance)
        }
    }

    /** Calls run method on given script, get the resulting css, calls doProcessing, the calls
     * {@link #addIfResultIsGrooCSS(java.lang.Object, org.groocss.MediaCSS)} and returns the css.
     * @param script Script to run.
     * @return The root {@link MediaCSS} element.
     */
    protected static MediaCSS runAndProcessScript(Script script) {
        def result = script.run()
        MediaCSS css = (MediaCSS) script.getProperty('css')

        css.doProcessing()
        addIfResultIsGrooCSS(result, css)

        css
    }

    /** If the given result is a GrooCSS object its inner stuff will be added to given css.
     *
     * @param result Object returned from a script (could be anything, even null).
     * @param css The root {@link MediaCSS} element.
     */
    protected static void addIfResultIsGrooCSS(result, MediaCSS css) {
        if (result instanceof GrooCSS && ((GrooCSS) result).css.mediaRule == null) {
            MediaCSS resultCss = ((GrooCSS) result).css

            css << resultCss.fonts
            css << resultCss.otherCss
            css << resultCss.kfs
            css << resultCss.groups
        }
    }

    /** Processes a given InputStream and outputs to given OutputStream assuming input script returns a GrooCSS. */
    @TypeChecked
    static void convertWithoutBase(InputStream inf, OutputStream out, String charset1 = "UTF-8",
                                   Map<String, Object> variables = null) {
        out.withPrintWriter { pw ->
            convertWithoutBase new InputStreamReader(inf, charset1), pw, variables
        }
    }

    /** Processes a given Reader and outputs to given PrintWriter assuming input script returns a GrooCSS. */
    @TypeChecked
    static void convertWithoutBase(Reader reader, PrintWriter writer, Map<String, Object> variables = null) {
        GroovyShell shell = makeShell(false)
        reader.withCloseable { input ->
            def script = shell.parse(input)
            if (variables) variables.each { key, value -> script.binding.setVariable(key, value) }
            GrooCSS result = (GrooCSS) script.run()
            assert result
            writer.withCloseable { pw ->
                result.css.writeTo pw
                pw.flush()
            }
        }
    }

    /** Processes a given InputStream and outputs to given OutputStream. */
    static void process(Config conf = new Config(), InputStream ins, OutputStream out) { convert conf, ins, out }

    /** Processes a given Reader and outputs to given PrintWriter. */
    static void process(Config conf = new Config(), Reader reader, PrintWriter writer) { convert conf, reader, writer }

    /** Processes a given groocss string and outputs as CSS string.
     * @param conf Optional Config object for configuration.
     * @param groocss Input String with GrooCSS code.
     * @param charset1 Charset of file to read (UTF-8 by default).
     * @param addMeta Tells GrooCSS to augment String and Integer classes using metaClass (true by default).
     */
    @TypeChecked
    static String process(Config conf = new Config(), String groocss, String charset1 = "UTF-8", boolean addMeta=true) {
        convert conf, groocss, charset1, addMeta
    }

    @InheritConstructors
    @CompileStatic
    static class Configurer extends Config {

        Configurer convert(File inf, File out) {
            GrooCSS.convert(this, inf, out)
            this
        }

        /** Processes a given InputStream and outputs to given OutputStream. */
        Configurer convert(InputStream ins, OutputStream out, String charset1 = null) {
            charset1 ? GrooCSS.convert(this, ins, out, charset1) : GrooCSS.convert(this, ins, out)
            this
        }

        /** Processes a given Reader and outputs to given PrintWriter. */
        Configurer convert(Reader reader, PrintWriter writer) { GrooCSS.convert(this, reader, writer); this }

        /** Processes a given groocss string and outputs as CSS string. */
        String convert(String groocss, String charset1 = null) {
            charset1 ? GrooCSS.convert(this, groocss, charset1) : GrooCSS.convert(this, groocss)
        }

        /** Processes the given closure with built config. */
        GrooCSS process(@DelegatesTo(value = GrooCSS, strategy = Closure.DELEGATE_FIRST) Closure clos) { GrooCSS.runBlock(this, clos) }

        /** Processes a given InputStream and outputs to given OutputStream. */
        Configurer process(InputStream ins, OutputStream out, String charset1 = null) { convert ins, out, charset1 }

        /** Processes a given Reader and outputs to given PrintWriter. */
        Configurer process(Reader reader, PrintWriter writer) { convert reader, writer }

        /** Processes a given File and outputs to given out File. */
        Configurer process(File inf, File out) { convert inf, out }

        /** Processes a given groocss string and outputs as CSS string. */
        String process(String groocss, String charset1 = null) { convert groocss, charset1 }

        /** Processes the given closure with built config. */
        GrooCSS runBlock(@DelegatesTo(value = GrooCSS, strategy = Closure.DELEGATE_FIRST) Closure clos) { GrooCSS.runBlock(this, clos) }
    }

    static Configurer withConfig(@DelegatesTo(Configurer) Closure<Configurer> closure) {
        Configurer c = new Configurer()
        closure.delegate = c
        closure(c)
        c
    }

    static Configurer withProperties(Properties properties) {
        new Configurer(properties)
    }

    static Configurer withPropertiesFile(String filePath) {
        withPropertiesFile(new File(filePath))
    }

    static Configurer withPropertiesFile(File file) {
        new Configurer(file)
    }

    /** Main config. */
    Config config = new Config()

    /** Main MediaCSS root.*/
    MediaCSS css = new MediaCSS(this, this.config)

    /** Makes sure that config passes through to root css. */
    void setConfig(Config config1) { this.config = css.config = config1 }

    /** Current MediaCSS object used for processing. */
    MediaCSS currentCss = css

    GrooCSS() {
        threadLocalInstance.set(this) // set this instance for the current Thread
    }

    /** Implements getting variables for process or runBlock type invocations. */
    def propertyMissing(String name) {
        def value = config.variables?.get(name)
        if (value == null) throw new MissingPropertyException(name)
        else value
    }

    /** Called when addMeta is true (parameter to "convert") which is used by Gradle plugin. */
    @CompileDynamic
    static void initMetaClasses(boolean addNumberMeta = true, boolean addStringMeta = true) {
        if (addNumberMeta) addNumberMetaStuff()
        if (addStringMeta) addStringMetaStuff()
        Integer.metaClass.initMetaClassesCalled = {return true}
    }

    @CompileDynamic
    private static void addStringMetaStuff() {
        String.metaClass.groocss = { Config config, Closure closure ->
            println "processing $delegate"; GrooCSS.process(config, closure)
        }
        String.metaClass.groocss = { Closure closure ->
            println "processing $delegate"; GrooCSS.process(closure)
        }
        String.metaClass.getUrl = { 'url(' + delegate + ')' }
        String.metaClass.getColor = { new Color(delegate) }
        String.metaClass.toColor = { new Color(delegate) }
        String.metaClass.sg { Closure closure -> StringExtension.sg(delegate, closure) }
        String.metaClass.id = { Closure closure -> StringExtension.id(delegate, closure) }
        String.metaClass.$ = { Closure closure -> StringExtension.sg(delegate, closure) }
        String.metaClass.kf = { Closure closure -> StringExtension.keyframes(delegate, closure) }
        String.metaClass.keyframes = { Closure closure -> StringExtension.keyframes(delegate, closure) }
        String.metaClass.media = { Closure closure -> StringExtension.media(delegate, closure) }
    }

    @CompileDynamic
    private static void addNumberMetaStuff() {
        Number.metaClass.propertyMissing = { new Measurement(delegate, "$it") }
        Number.metaClass.getPercent = { new Measurement((Number) delegate, '%') }
        Number.metaClass.mod = { Underscore u -> new Measurement((Number) delegate, '%') }
        Number.metaClass.getColor = { new Color((Number) delegate) }
        Number.metaClass.toColor = { new Color((Number) delegate) }
        /** Used within keyframes block such as 50% { opacity: 1 }. */
        Integer.metaClass.mod = { Closure frameCl -> NumberExtension.mod((Integer) delegate, frameCl) }
    }

    GrooCSS(Config config) {
        this()
        this.config = config
    }

    String toString() { css.toString() }

    MediaCSS media(String mediaRule, @DelegatesTo(GrooCSS) Closure clos) {
        MediaCSS mcss = new MediaCSS(this, mediaRule, this.config)
        MediaCSS oldCss = currentCss
        currentCss = mcss
        clos.delegate = this
        clos(mcss)
        oldCss.add mcss
        currentCss = oldCss
        mcss
    }

    /** Calls {@link #kf(java.lang.String, groovy.lang.Closure)}. */
    KeyFrames keyframes(String name, @DelegatesTo(KeyFrames) Closure clos) {
        kf(name, clos)
    }

    /** Creates a new KeyFrames element and runs given closure on it. */
    KeyFrames kf(String name, @DelegatesTo(KeyFrames) Closure clos) {
        KeyFrames frames = currentKf = new KeyFrames(name: name, config: currentCss.config)
        clos.delegate = frames
        clos(frames)
        currentKf = null
        currentCss << frames
        frames
    }

    /** Creates a new StyleGroup element and runs given closure on it. */
    StyleGroup sel(String selector, 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure<StyleGroup> clos) {
        currentCss.sel(selector, clos)
    }

    /** Creates a selector from any given argument. */
    Selector sel(selector) {
        new Selector("$selector", currentCss)
    }

    /** Creates a selector from any given argument. Same as {@link #sel(java.lang.Object)}. */
    Selector $(selector) {
        sel(selector)
    }

    /** Creates a new StyleGroup element and runs given closure on it. */
    StyleGroup sel(Selector selector,
                   @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure<StyleGroup> clos) {
        currentCss.sel("$selector", clos)
    }

    /** Creates an unattached StyleGroup object, useful for adding Styles to a StyleGroup conditionally
     * or for reusing a group of styles several times. */
    StyleGroup styles(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure<StyleGroup> clos) {
        currentCss.sel('', clos, false)
    }

    /** Creates a new @font-face element and runs given closure on it. */
    FontFace fontFace(@DelegatesTo(value = FontFace, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        FontFace ff = new FontFace()
        clos.delegate = ff
        clos.resolveStrategy = Closure.DELEGATE_FIRST
        clos(ff)
        currentCss.add ff
        ff
    }

    /** Creates a new StyleGroup element and runs given closure on it. */
    StyleGroup sg(String selector, @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sel(selector, clos)
    }

    /** Creates a new StyleGroup element and runs given closure on it. */
    StyleGroup $(String selector, @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sel(selector, clos)
    }

    /** Creates a new StyleGroup element and runs given closure on it. */
    StyleGroup sg(Selector selector, @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sel(selector, clos)
    }

    /** Creates a new StyleGroup element, with all selectors in the given list joined with commas.
     * If given list is empty, this method has the same behaviour as styles(closure). */
    @CompileDynamic
    StyleGroup sg(List selectors, @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        if (selectors.isEmpty()) {
            return styles(clos)
        }
        sg(selectors.tail().inject(selectors[0]) {a,b -> a|b}, clos)
    }

    Style style(@DelegatesTo(Style) Closure clos) {
        Style s = new Style()
        clos.delegate = s
        clos()
        s
    }

    /** Creates a Style with given name and value. */
    Style style(String name, Object value) {
        new Style(name: name, value: "$value")
    }

    @Delegate
    final ColorMethods colorMethods = new ColorMethods()

    def run() {}

    /** Processes the given closure with given optional config. Calls runBlock.
     * @see #runBlock(org.groocss.Config, groovy.lang.Closure, boolean)
     */
    static GrooCSS process(Config config = null,
                           @DelegatesTo(value = GrooCSS, strategy = Closure.DELEGATE_FIRST) Closure closure,
                           boolean addMeta = true) {
        runBlock(config, closure, addMeta)
    }

    /**
     * Processes the given closure with given optional config. If a GrooCSS instance
     * is set to the threadLocalInstance (threadLocalInstance.get() != null) its Config will be used
     * if the given config parameter is null. If no Config is found, the default Config is used.
     *
     * @param config Config to use (if null it uses Config from current ThreadLocal if any or default Config otherwise).
     * @param closure The Closure containing the DSL to run.
     * @param addMeta Whether to add metaClass methods to String and Integer classes (true by default).
     */
    static GrooCSS runBlock(Config config = null,
                            @DelegatesTo(value = GrooCSS, strategy = Closure.DELEGATE_FIRST) Closure closure,
                            boolean addMeta = true) {
        final Config config2
        final GrooCSS previousGrooCssInstance = threadLocalInstance.get()

        if (config == null && previousGrooCssInstance?.config != null) config2 = previousGrooCssInstance.config
        else if (config) config2 = config
        else config2 = new Config()

        GrooCSS gcss = new GrooCSS(config: config2)
        if (addMeta) GrooCSS.initMetaClasses()
        gcss.css.config = config2
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = gcss
        closure()
        gcss.css.doProcessing()
        resetThreadLocalInstance(previousGrooCssInstance)
        gcss
    }

    /** Sets the threadLocalInstance to the given GrooCSS instance. */
    protected static void resetThreadLocalInstance(final GrooCSS previousGrooCssInstance) {
        threadLocalInstance.set(previousGrooCssInstance)
    }

    /** Writes the CSS to the given file. */
    void writeTo(File f) {
        f.withPrintWriter { pw -> css.writeTo pw }
    }

    /** Writes the CSS to the given file. */
    void writeToFile(String filename) {
        writeTo(new File(filename))
    }

    void charset(String charset) {
        this.config.charset = charset
    }
    String getUtf8() { 'UTF-8' }
    String getUtf16() { 'UTF-16' }
    String getIso8859() { 'ISO-8859-1' }

    //------------------------------------------------------------------> HTML5 elements
    /** Math element. */
    StyleGroup math(String sel='', 
                    @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('math' + sel, clos) }

    /** Scalable vector graphics. */
    StyleGroup svg(String sel='', 
                   @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('svg' + sel, clos) }

    /** Hyperlink. */
    StyleGroup a(String sel='', 
                 @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('a' + sel, clos) }

    /** Hyperlink a:hover. */
    StyleGroup a_hover(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('a:hover' + sel, clos) }

    /** Hyperlink a:focus. */
    StyleGroup a_focus(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('a:focus' + sel, clos) }

    /** Hyperlink a:active. */
    StyleGroup a_active(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('a:active' + sel, clos) }

    /** Hyperlink a:visited. */
    StyleGroup a_visited(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('a:visited' + sel, clos) }

    /** Abbreviation. */
    StyleGroup abbr(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('abbr' + sel, clos) }

    /** Contact information. */
    StyleGroup address(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('address' + sel, clos) }

    /** Image-map hyperlink. */
    StyleGroup area(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('area' + sel, clos) }

    /** Article. */
    StyleGroup article(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('article' + sel, clos) }

    /** Tangential content. */
    StyleGroup aside(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('aside' + sel, clos) }

    /** Audio stream. */
    StyleGroup audio(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('audio' + sel, clos) }

    /** Offset text conventionally styled in bold. */
    StyleGroup b(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('b' + sel, clos) }

    /** Base URL. */
    StyleGroup base(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('base' + sel, clos) }

    /** BiDi isolate. */
    StyleGroup bdi(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('bdi' + sel, clos) }

    /** BiDi override. */
    StyleGroup bdo(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('bdo' + sel, clos) }

    /** Block quotation. */
    StyleGroup blockquote(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('blockquote' + sel, clos) }

    /** Document body. */
    StyleGroup body(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('body' + sel, clos) }

    /** Line break. */
    StyleGroup br(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('br' + sel, clos) }

    /** Button. */
    StyleGroup button(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('button' + sel, clos) }

    /** Submit button. */
    StyleGroup buttonSubmit(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('button [type="submit"]' + sel, clos) }

    /** Reset button. */
    StyleGroup buttonReset(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('button [type="reset"]' + sel, clos) }

    /** Button with no additional semantics. */
    StyleGroup buttonButton(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('button [type="button"]' + sel, clos) }

    /** Canvas for dynamic graphics. */
    StyleGroup canvas(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('canvas' + sel, clos) }

    /** Table title. */
    StyleGroup caption(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('caption' + sel, clos) }

    /** Cited title of a work. */
    StyleGroup cite(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('cite' + sel, clos) }

    /** Code fragment. */
    StyleGroup code(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('code' + sel, clos) }

    /** Table column. */
    StyleGroup col(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('col' + sel, clos) }

    /** Table column group. */
    StyleGroup colgroup(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('colgroup' + sel, clos) }

    /** Command. */
    StyleGroup command(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('command' + sel, clos) }

    /** Command with an associated action. */
    StyleGroup commandCommand(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('command [type="command"]' + sel, clos) }

    /** Selection of one item from a list of items. */
    StyleGroup commandRadio(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('command [type="radio"]' + sel, clos) }

    /** State or option that can be toggled. */
    StyleGroup commandCheckbox(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('command [type="checkbox"]' + sel, clos) }

    /** Predefined options for other controls. */
    StyleGroup datalist(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('datalist' + sel, clos) }

    /** Description or value. */
    StyleGroup dd(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('dd' + sel, clos) }

    /** Deleted text. */
    StyleGroup del(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('del' + sel, clos) }

    /** Control for additional on-demand information. */
    StyleGroup details(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('details' + sel, clos) }

    /** Defining instance. */
    StyleGroup dfn(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('dfn' + sel, clos) }

    /** Defines a dialog box or window*/
    StyleGroup dialog(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('dialog' + sel, clos) }

    /** Generic flow container. */
    StyleGroup div(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('div' + sel, clos) }

    /** Description list. */
    StyleGroup dl(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('dl' + sel, clos) }

    /** Term or name. */
    StyleGroup dt(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('dt' + sel, clos) }

    /** Emphatic stress. */
    StyleGroup em(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('em' + sel, clos) }

    /** Integration point for plugins. */
    StyleGroup embed(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('embed' + sel, clos) }

    /** Set of related form controls. */
    StyleGroup fieldset(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('fieldset' + sel, clos) }

    /** Figure caption. */
    StyleGroup figcaption(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('figcaption' + sel, clos) }

    /** Figure with optional caption. */
    StyleGroup figure(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('figure' + sel, clos) }

    /** Footer. */
    StyleGroup footer(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('footer' + sel, clos) }

    /** User-submittable form. */
    StyleGroup form(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('form' + sel, clos) }

    /** Heading. */
    StyleGroup h1(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('h1' + sel, clos) }

    /** Heading. */
    StyleGroup h2(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('h2' + sel, clos) }

    /** Heading. */
    StyleGroup h3(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('h3' + sel, clos) }

    /** Heading. */
    StyleGroup h4(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('h4' + sel, clos) }

    /** Heading. */
    StyleGroup h5(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('h5' + sel, clos) }

    /** Heading. */
    StyleGroup h6(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('h6' + sel, clos) }

    /** Document metadata container. */
    StyleGroup head(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('head' + sel, clos) }

    /** Header. */
    StyleGroup header(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('header' + sel, clos) }

    /** Heading group. */
    StyleGroup hgroup(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('hgroup' + sel, clos) }

    /** Thematic break. */
    StyleGroup hr(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('hr' + sel, clos) }

    /** Root element. */
    StyleGroup html(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('html' + sel, clos) }

    /** Offset text conventionally styled in italic. */
    StyleGroup i(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('i' + sel, clos) }

    /** Nested browsing context (inline frame). */
    StyleGroup iframe(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('iframe' + sel, clos) }

    /** Image. */
    StyleGroup img(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('img' + sel, clos) }

    /** Input control. */
    StyleGroup input(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('input' + sel, clos) }

    /** Text-input field. */
    StyleGroup inputText(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('input [type="text"]' + sel, clos) }

    /** Password-input field. */
    StyleGroup inputPassword(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="password"]' + sel, clos) }

    /** Checkbox. */
    StyleGroup inputCheckbox(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="checkbox"]' + sel, clos) }

    /** Radio button. */
    StyleGroup inputRadio(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="radio"]' + sel, clos) }

    /** Button. */
    StyleGroup inputButton(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="button"]' + sel, clos) }

    /** Submit button. */
    StyleGroup inputSubmit(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="submit"]' + sel, clos) }

    /** Reset button. */
    StyleGroup inputReset(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="reset"]' + sel, clos) }

    /** File upload control. */
    StyleGroup inputFile(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="file"]' + sel, clos) }

    /** Hidden input control. */
    StyleGroup inputHidden(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="hidden"]' + sel, clos) }

    /** Image-coordinates input control. */
    StyleGroup inputImage(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="image"]' + sel, clos) }

    /** Global date-and-time input control. */
    StyleGroup inputDatetime(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="datetime"]' + sel, clos) }

    /** Local date-and-time input control. */
    StyleGroup inputDatetimeLocal(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="datetime-local"]' + sel, clos) }

    /** Date input control. */
    StyleGroup inputDate(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="date"]' + sel, clos) }

    /** Year-and-month input control. */
    StyleGroup inputMonth(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="month"]' + sel, clos) }

    /** Time input control. */
    StyleGroup inputTime(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="time"]' + sel, clos) }

    /** Year-and-week input control. */
    StyleGroup inputWeek(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="week"]' + sel, clos) }

    /** Number input control. */
    StyleGroup inputNumber(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="number"]' + sel, clos) }

    /** Imprecise number-input control. */
    StyleGroup inputRange(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="range"]' + sel, clos) }

    /** E-mail address input control. */
    StyleGroup inputEmail(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="email"]' + sel, clos) }

    /** URL input control. */
    StyleGroup inputUrl(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="url"]' + sel, clos) }

    /** Search field. */
    StyleGroup inputSearch(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) {
        sg('input [type="search"]' + sel, clos) }

    /** Telephone-number-input field. */
    StyleGroup inputTel(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('input [type="tel"]' + sel, clos) }

    /** Color-well control. */
    StyleGroup inputColor(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos){ sg('input [type="color"]' + sel, clos)}

    /** Inserted text. */
    StyleGroup ins(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('ins' + sel, clos) }

    /** User input. */
    StyleGroup kbd(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('kbd' + sel, clos) }

    /** Key-pair generator/input control. */
    StyleGroup keygen(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('keygen' + sel, clos) }

    /** Caption for a form control. */
    StyleGroup label(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('label' + sel, clos) }

    /** Title or explanatory caption. */
    StyleGroup legend(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('legend' + sel, clos) }

    /** List item. */
    StyleGroup li(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('li' + sel, clos) }

    /** Inter-document relationship metadata. */
    StyleGroup link(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('link' + sel, clos) }

    /** Main definition. */
    StyleGroup main(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('main' + sel, clos) }

    /** Image-map definition. */
    StyleGroup map(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('map' + sel, clos) }

    /** Marked (highlighted) text. */
    StyleGroup mark(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('mark' + sel, clos) }

    /** List of commands. */
    StyleGroup menu(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('menu' + sel, clos) }

    /** Scalar gauge. */
    StyleGroup meter(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('meter' + sel, clos) }

    /** Group of navigational links. */
    StyleGroup nav(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('nav' + sel, clos) }

    /** Fallback content for script. */
    StyleGroup noscript(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('noscript' + sel, clos) }

    /** Generic external content. */
    StyleGroup object(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('object' + sel, clos) }

    /** Ordered list. */
    StyleGroup ol(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('ol' + sel, clos) }

    /** Group of options. */
    StyleGroup optgroup(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('optgroup' + sel, clos) }

    /** Option. */
    StyleGroup option(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('option' + sel, clos) }

    /** Result of a calculation in a form. */
    StyleGroup output(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('output' + sel, clos) }

    /** Paragraph. */
    StyleGroup p(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('p' + sel, clos) }

    /** Initialization parameters for plugins. */
    StyleGroup param(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('param' + sel, clos) }

    /** Preformatted text. */
    StyleGroup pre(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('pre' + sel, clos) }

    /** Progress indicator. */
    StyleGroup progress(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('progress' + sel, clos) }

    /** Quoted text. */
    StyleGroup q(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('q' + sel, clos) }

    /** Ruby parenthesis. */
    StyleGroup rp(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('rp' + sel, clos) }

    /** Ruby text. */
    StyleGroup rt(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('rt' + sel, clos) }

    /** Ruby annotation. */
    StyleGroup ruby(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('ruby' + sel, clos) }

    /** Struck text. */
    StyleGroup s(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('s' + sel, clos) }

    /** (sample) output. */
    StyleGroup samp(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('samp' + sel, clos) }

    /** Section. */
    StyleGroup section(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('section' + sel, clos) }

    /** Option-selection form control. */
    StyleGroup select(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('select' + sel, clos) }

    /** Small print. */
    StyleGroup small(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('small' + sel, clos) }

    /** Media source. */
    StyleGroup source(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('source' + sel, clos) }

    /** Generic span. */
    StyleGroup span(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('span' + sel, clos) }

    /** Strong importance. */
    StyleGroup strong(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('strong' + sel, clos) }

    /** Subscript. */
    StyleGroup sub(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('sub' + sel, clos) }

    /** Summary, caption, or legend for a details control. */
    StyleGroup summary(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('summary' + sel, clos) }

    /** Superscript. */
    StyleGroup sup(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('sup' + sel, clos) }

    /** Table. */
    StyleGroup table(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('table' + sel, clos) }

    /** Table row group. */
    StyleGroup tbody(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('tbody' + sel, clos) }

    /** Table cell. */
    StyleGroup td(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('td' + sel, clos) }

    /** Text input area. */
    StyleGroup textarea(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('textarea' + sel, clos) }

    /** Table footer row group. */
    StyleGroup tfoot(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('tfoot' + sel, clos) }

    /** Table header cell. */
    StyleGroup th(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('th' + sel, clos) }

    /** Table heading group. */
    StyleGroup thead(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('thead' + sel, clos) }

    /** Date and/or time. */
    StyleGroup time(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('time' + sel, clos) }

    /** Document title. */
    StyleGroup title(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('title' + sel, clos) }

    /** Table row. */
    StyleGroup tr(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('tr' + sel, clos) }

    /** Supplementary media track. */
    StyleGroup track(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('track' + sel, clos) }

    /** Offset text conventionally styled with an underline. */
    StyleGroup u(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('u' + sel, clos) }

    /** Unordered list. */
    StyleGroup ul(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('ul' + sel, clos) }

    /** Variable or placeholder text. */
    StyleGroup var(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('var' + sel, clos) }

    /** Video. */
    StyleGroup video(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('video' + sel, clos) }

    /** Line-break opportunity. */
    StyleGroup wbr(String sel='', 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { sg('wbr' + sel, clos) }

    /** The following 110 methods enable CSS-like selectors like body a, b {} or div ~ ul img {}
     * or extend(body a) and many others. */

    protected Selectable handleHtmlElement(String element, Selectable... sels) {
        if (sels.length == 0) {
            new Selector(element, currentCss)
        } else if (sels.length == 1) {
            sels[0].resetSelector("$element ${sels[0].selector}")
        } else if (sels[-1] instanceof StyleGroup) {
            sels[-1].resetSelector("$element ${sels[0..-2].join(', ')}, ${sels[-1].selector}")
        } else {
            new Selector("$element ${sels.join(', ')}", currentCss)
        }
    }

    /** Math element. */
    Selectable math(Selectable... sel) { handleHtmlElement('math', sel) }

    /** Scalable vector graphics. */
    Selectable svg(Selectable... sel) { handleHtmlElement('svg', sel) }

    /** Hyperlink. */
    Selectable a(Selectable... sel) { handleHtmlElement('a', sel) }

    /** Abbreviation. */
    Selectable abbr(Selectable... sel) { handleHtmlElement('abbr', sel) }

    /** Contact information. */
    Selectable address(Selectable... sel) { handleHtmlElement('address', sel) }

    /** Image-map hyperlink. */
    Selectable area(Selectable... sel) { handleHtmlElement('area', sel) }

    /** Article. */
    Selectable article(Selectable... sel) { handleHtmlElement('article', sel) }

    /** Tangential content. */
    Selectable aside(Selectable... sel) { handleHtmlElement('aside', sel) }

    /** Audio stream. */
    Selectable audio(Selectable... sel) { handleHtmlElement('audio', sel) }

    /** Offset text conventionally styled in bold. */
    Selectable b(Selectable... sel) { handleHtmlElement('b', sel) }

    /** Base URL. */
    Selectable base(Selectable... sel) { handleHtmlElement('base', sel) }

    /** BiDi isolate. */
    Selectable bdi(Selectable... sel) { handleHtmlElement('bdi', sel) }

    /** BiDi override. */
    Selectable bdo(Selectable... sel) { handleHtmlElement('bdo', sel) }

    /** Block quotation. */
    Selectable blockquote(Selectable... sel) { handleHtmlElement('blockquote', sel) }

    /** Document body. */
    Selectable body(Selectable... sel) { handleHtmlElement('body', sel) }

    /** Line break. */
    Selectable br(Selectable... sel) { handleHtmlElement('br', sel) }

    /** Button. */
    Selectable button(Selectable... sel) { handleHtmlElement('button', sel) }

    /** Canvas for dynamic graphics. */
    Selectable canvas(Selectable... sel) { handleHtmlElement('canvas', sel) }

    /** Table title. */
    Selectable caption(Selectable... sel) { handleHtmlElement('caption', sel) }

    /** Cited title of a work. */
    Selectable cite(Selectable... sel) { handleHtmlElement('cite', sel) }

    /** Code fragment. */
    Selectable code(Selectable... sel) { handleHtmlElement('code', sel) }

    /** Table column. */
    Selectable col(Selectable... sel) { handleHtmlElement('col', sel) }

    /** Table column group. */
    Selectable colgroup(Selectable... sel) { handleHtmlElement('colgroup', sel) }

    /** Command. */
    Selectable command(Selectable... sel) { handleHtmlElement('command', sel) }

    /** Predefined options for other controls. */
    Selectable datalist(Selectable... sel) { handleHtmlElement('datalist', sel) }

    /** Description or value. */
    Selectable dd(Selectable... sel) { handleHtmlElement('dd', sel) }

    /** Deleted text. */
    Selectable del(Selectable... sel) { handleHtmlElement('del', sel) }

    /** Control for additional on-demand information. */
    Selectable details(Selectable... sel) { handleHtmlElement('details', sel) }

    /** Defining instance. */
    Selectable dfn(Selectable... sel) { handleHtmlElement('dfn', sel) }

    /** Defines a dialog box or window. */
    Selectable dialog(Selectable... sel) { handleHtmlElement('dialog', sel) }

    /** Generic flow container. */
    Selectable div(Selectable... sel) { handleHtmlElement('div', sel) }

    /** Description list. */
    Selectable dl(Selectable... sel) { handleHtmlElement('dl', sel) }

    /** Term or name. */
    Selectable dt(Selectable... sel) { handleHtmlElement('dt', sel) }

    /** Emphatic stress. */
    Selectable em(Selectable... sel) { handleHtmlElement('em', sel) }

    /** Integration point for plugins. */
    Selectable embed(Selectable... sel) { handleHtmlElement('embed', sel) }

    /** Set of related form controls. */
    Selectable fieldset(Selectable... sel) { handleHtmlElement('fieldset', sel) }

    /** Figure caption. */
    Selectable figcaption(Selectable... sel) { handleHtmlElement('figcaption', sel) }

    /** Figure with optional caption. */
    Selectable figure(Selectable... sel) { handleHtmlElement('figure', sel) }

    /** Footer. */
    Selectable footer(Selectable... sel) { handleHtmlElement('footer', sel) }

    /** User-submittable form. */
    Selectable form(Selectable... sel) { handleHtmlElement('form', sel) }

    /** Heading. */
    Selectable h1(Selectable... sel) { handleHtmlElement('h1', sel) }

    /** Heading. */
    Selectable h2(Selectable... sel) { handleHtmlElement('h2', sel) }

    /** Heading. */
    Selectable h3(Selectable... sel) { handleHtmlElement('h3', sel) }

    /** Heading. */
    Selectable h4(Selectable... sel) { handleHtmlElement('h4', sel) }

    /** Heading. */
    Selectable h5(Selectable... sel) { handleHtmlElement('h5', sel) }

    /** Heading. */
    Selectable h6(Selectable... sel) { handleHtmlElement('h6', sel) }

    /** Document metadata container. */
    Selectable head(Selectable... sel) { handleHtmlElement('head', sel) }

    /** Header. */
    Selectable header(Selectable... sel) { handleHtmlElement('header', sel) }

    /** Heading group. */
    Selectable hgroup(Selectable... sel) { handleHtmlElement('hgroup', sel) }

    /** Thematic break. */
    Selectable hr(Selectable... sel) { handleHtmlElement('hr', sel) }

    /** Root element. */
    Selectable html(Selectable... sel) { handleHtmlElement('html', sel) }

    /** Offset text conventionally styled in italic. */
    Selectable i(Selectable... sel) { handleHtmlElement('i', sel) }

    /** Nested browsing context (inline frame). */
    Selectable iframe(Selectable... sel) { handleHtmlElement('iframe', sel) }

    /** Image. */
    Selectable img(Selectable... sel) { handleHtmlElement('img', sel) }

    /** Input control. */
    Selectable input(Selectable... sel) { handleHtmlElement('input', sel) }

    /** Inserted text. */
    Selectable ins(Selectable... sel) { handleHtmlElement('ins', sel) }

    /** User input. */
    Selectable kbd(Selectable... sel) { handleHtmlElement('kbd', sel) }

    /** Key-pair generator/input control. */
    Selectable keygen(Selectable... sel) { handleHtmlElement('keygen', sel) }

    /** Caption for a form control. */
    Selectable label(Selectable... sel) { handleHtmlElement('label', sel) }

    /** Title or explanatory caption. */
    Selectable legend(Selectable... sel) { handleHtmlElement('legend', sel) }

    /** List item. */
    Selectable li(Selectable... sel) { handleHtmlElement('li', sel) }

    /** Inter-document relationship metadata. */
    Selectable link(Selectable... sel) { handleHtmlElement('link', sel) }

    /** Main definition. */
    Selectable main(Selectable... sel) { handleHtmlElement('main', sel) }

    /** Image-map definition. */
    Selectable map(Selectable... sel) { handleHtmlElement('map', sel) }

    /** Marked (highlighted) text. */
    Selectable mark(Selectable... sel) { handleHtmlElement('mark', sel) }

    /** List of commands. */
    Selectable menu(Selectable... sel) { handleHtmlElement('menu', sel) }

    /** Scalar gauge. */
    Selectable meter(Selectable... sel) { handleHtmlElement('meter', sel) }

    /** Group of navigational links. */
    Selectable nav(Selectable... sel) { handleHtmlElement('nav', sel) }

    /** Fallback content for script. */
    Selectable noscript(Selectable... sel) { handleHtmlElement('noscript', sel) }

    /** Generic external content. */
    Selectable object(Selectable... sel) { handleHtmlElement('object', sel) }

    /** Ordered list. */
    Selectable ol(Selectable... sel) { handleHtmlElement('ol', sel) }

    /** Group of options. */
    Selectable optgroup(Selectable... sel) { handleHtmlElement('optgroup', sel) }

    /** Option. */
    Selectable option(Selectable... sel) { handleHtmlElement('option', sel) }

    /** Result of a calculation in a form. */
    Selectable output(Selectable... sel) { handleHtmlElement('output', sel) }

    /** Paragraph. */
    Selectable p(Selectable... sel) { handleHtmlElement('p', sel) }

    /** Initialization parameters for plugins. */
    Selectable param(Selectable... sel) { handleHtmlElement('param', sel) }

    /** Preformatted text. */
    Selectable pre(Selectable... sel) { handleHtmlElement('pre', sel) }

    /** Progress indicator. */
    Selectable progress(Selectable... sel) { handleHtmlElement('progress', sel) }

    /** Quoted text. */
    Selectable q(Selectable... sel) { handleHtmlElement('q', sel) }

    /** Ruby parenthesis. */
    Selectable rp(Selectable... sel) { handleHtmlElement('rp', sel) }

    /** Ruby text. */
    Selectable rt(Selectable... sel) { handleHtmlElement('rt', sel) }

    /** Ruby annotation. */
    Selectable ruby(Selectable... sel) { handleHtmlElement('ruby', sel) }

    /** Struck text. */
    Selectable s(Selectable... sel) { handleHtmlElement('s', sel) }

    /** (sample) output. */
    Selectable samp(Selectable... sel) { handleHtmlElement('samp', sel) }

    /** Section. */
    Selectable section(Selectable... sel) { handleHtmlElement('section', sel) }

    /** Option-selection form control. */
    Selectable select(Selectable... sel) { handleHtmlElement('select', sel) }

    /** Small print. */
    Selectable small(Selectable... sel) { handleHtmlElement('small', sel) }

    /** Media source. */
    Selectable source(Selectable... sel) { handleHtmlElement('source', sel) }

    /** Generic span. */
    Selectable span(Selectable... sel) { handleHtmlElement('span', sel) }

    /** Strong importance. */
    Selectable strong(Selectable... sel) { handleHtmlElement('strong', sel) }

    /** Subscript. */
    Selectable sub(Selectable... sel) { handleHtmlElement('sub', sel) }

    /** Summary, caption, or legend for a details control. */
    Selectable summary(Selectable... sel) { handleHtmlElement('summary', sel) }

    /** Superscript. */
    Selectable sup(Selectable... sel) { handleHtmlElement('sup', sel) }

    /** Table. */
    Selectable table(Selectable... sel) { handleHtmlElement('table', sel) }

    /** Table row group. */
    Selectable tbody(Selectable... sel) { handleHtmlElement('tbody', sel) }

    /** Table cell. */
    Selectable td(Selectable... sel) { handleHtmlElement('td', sel) }

    /** Text input area. */
    Selectable textarea(Selectable... sel) { handleHtmlElement('textarea', sel) }

    /** Table footer row group. */
    Selectable tfoot(Selectable... sel) { handleHtmlElement('tfoot', sel) }

    /** Table header cell. */
    Selectable th(Selectable... sel) { handleHtmlElement('th', sel) }

    /** Table heading group. */
    Selectable thead(Selectable... sel) { handleHtmlElement('thead', sel) }

    /** Date and/or time. */
    Selectable time(Selectable... sel) { handleHtmlElement('time', sel) }

    /** Document title. */
    Selectable title(Selectable... sel) { handleHtmlElement('title', sel) }

    /** Table row. */
    Selectable tr(Selectable... sel) { handleHtmlElement('tr', sel) }

    /** Supplementary media track. */
    Selectable track(Selectable... sel) { handleHtmlElement('track', sel) }

    /** Offset text conventionally styled with an underline. */
    Selectable u(Selectable... sel) { handleHtmlElement('u', sel) }

    /** Unordered list. */
    Selectable ul(Selectable... sel) { handleHtmlElement('ul', sel) }

    /** Variable or placeholder text. */
    Selectable var(Selectable... sel) { handleHtmlElement('var', sel) }

    /** Video. */
    Selectable video(Selectable... sel) { handleHtmlElement('video', sel) }

    /** Line-break opportunity. */
    Selectable wbr(Selectable... sel) { handleHtmlElement('wbr', sel) }


    //------------------------------------------------------------------> Math
    /** Returns the absolute value of a value.*/
    double abs(Number n) { (n instanceof Integer) ? n.abs() : Math.abs(n.doubleValue()) }

    /** Returns the arc tangent of a value; the returned angle is in the range -pi/2 through pi/2.*/
    double atan(Number n) { Math.atan(n.doubleValue()) }

    /** Returns the angle theta from the conversion of rectangular coordinates (x, y) to polar coordinates (r, theta).*/
    double atan2(Number y, Number x) { Math.atan2(y.doubleValue(), x.doubleValue()) }

    /** Returns the arc cosine of a value; the returned angle is in the range 0.0 through pi.*/
    double acos(Number n) { Math.acos(n.doubleValue()) }

    /** Returns the arc sine of a value; the returned angle is in the range -pi/2 through pi/2. */
    double asin(Number n) { Math.asin(n.doubleValue()) }

    /** Returns the trigonometric cosine of an angle (in radians).*/
    double cos(Number angle) { Math.cos(angle.doubleValue()) }

    /** Returns the trigonometric sine of an angle (in radians).*/
    double sin(Number angle) { Math.sin(angle.doubleValue()) }

    /** Returns the natural logarithm (base e) of a double value.*/
    double log(Number n) { Math.log(n.doubleValue()) }

    /** Returns the base 10 logarithm of a double value.*/
    double log10(Number n) { Math.log10(n.doubleValue()) }

    /** Returns the smallest (closest to negative infinity) double value that is greater than or equal to the argument
     * and is equal to a mathematical integer.*/
    int ceiling(Number n) { Math.ceil(n.doubleValue()) as int }

    /** Returns the largest (closest to positive infinity) double value that is less than or equal to the argument and
     * is equal to a mathematical integer.*/
    int floor(Number n) { Math.floor(n.doubleValue()) as int }

    /** Returns the value of the first argument raised to the power of the second argument.*/
    double pow(Number n, Number pow) { Math.pow(n.doubleValue(), pow.doubleValue()) }

    /** Returns the correctly rounded positive square root of a double value.*/
    double sqrt(Number n) { Math.sqrt(n.doubleValue()) }

    /** Returns the cube root of a double value.*/
    double cbrt(Number n) { Math.cbrt(n.doubleValue()) }

    /** Returns the trigonometric tangent of an angle (in radians).*/
    double tan(Number angle) { Math.tan(angle.doubleValue()) }

    /** Converts an angle measured in radians to an approximately equivalent angle measured in degrees.*/
    double toDegrees(Number angrad) { Measurement.toDegrees(angrad) }

    /**Converts an angle measured in degrees to an approximately equivalent angle measured in radians.*/
    double toRadians(Number angdeg) { Measurement.toRadians(angdeg) }

    //------------------------------------------------------------------> Units
    /** Returns units of a number. For example: em,px,mm,cm,ms,s. */
    String getUnit(value) {
        if (value instanceof Measurement) return value.unit
        def match = (value =~ /\d*\.?\d*(\w+)/)
        if(match.matches()) match?.group(1)
        else ''
    }

    /** Remove or change the unit of a dimension. */
    def unit(value, units = null) {
        if (units) {
            if (value instanceof Number) new Measurement(value, "$units")
            else "$value$units"
        }
        else {
            def match = (value =~ /(\d*\.?\d*)\w+/)
            if (match.matches()) {
                def num = match?.group(1)
                if (value ==~ /\d+/) num as Integer
                num as BigDecimal
            }
            else value
        }
    }

    /** Convert a number from one unit into another. Supports sizes (in,pt,pc,mm,cm,m), time (m,ms) and rad/deg. */
    def convert(value, units) {
        Number num = unit(value) as BigDecimal
        def conversion = getUnit(value) + "-$units"
        def converted = convertNum num, conversion

        "${stringify converted}$units"
    }

    /** Converts number to string in a sensible format. */
    static String stringify(Number converted) {
        converted.toString().contains("E") ? "${converted as Double}" : "$converted"
    }

    @TypeChecked
    Number convertNum(Number num, String conversion) {
        return Measurement.convertNum(num, conversion)
    }

    //------------------------------------------------------------------> Images
    Measurement getImageWidth(String filename) {
        def img = ImageIO.read new File(filename)
        new Measurement(img.width, 'px')
    }

    Measurement getImageHeight(String filename) {
        def img = ImageIO.read new File(filename)
        new Measurement(img.height, 'px')
    }

    String getImageSize(String filename) {
        def img = ImageIO.read new File(filename)
        "${img.width}px ${img.height}px"
    }

    //------------------------------------------------------------------> Elements
    Selector getMain() { newElement('main') }
    Selector getMath() { newElement('math') }
    Selector getSvg() { newElement('svg') }
    Selector getA() { newElement('a') }
    Selector getAbbr() { newElement('abbr') }
    Selector getAddress() { newElement('address') }
    Selector getArea() { newElement('area') }
    Selector getArticle() { newElement('article') }
    Selector getAside() { newElement('aside') }
    Selector getAudio() { newElement('audio') }
    Selector getB() { newElement('b') }
    Selector getBase() { newElement('base') }
    Selector getBdi() { newElement('bdi') }
    Selector getBdo() { newElement('bdo') }
    Selector getBlockquote() { newElement('blockquote') }
    Selector getBody() { newElement('body') }
    Selector getBr() { newElement('br') }
    Selector getButton() { newElement('button') }
    Selector getCanvas() { newElement('canvas') }
    Selector getCaption() { newElement('caption') }
    Selector getCite() { newElement('cite') }
    Selector getCode() { newElement('code') }
    Selector getCol() { newElement('col') }
    Selector getColgroup() { newElement('colgroup') }
    Selector getCommand() { newElement('command') }
    Selector getDatalist() { newElement('datalist') }
    Selector getDd() { newElement('dd') }
    Selector getDel() { newElement('del') }
    Selector getDetails() { newElement('details') }
    Selector getDialog() { newElement('dialog') }
    Selector getDfn() { newElement('dfn') }
    Selector getDiv() { newElement('div') }
    Selector getDl() { newElement('dl') }
    Selector getDt() { newElement('dt') }
    Selector getEm() { newElement('em') }
    Selector getEmbed() { newElement('embed') }
    Selector getFieldset() { newElement('fieldset') }
    Selector getFigcaption() { newElement('figcaption') }
    Selector getFigure() { newElement('figure') }
    Selector getFooter() { newElement('footer') }
    Selector getForm() { newElement('form') }
    Selector getH1() { newElement('h1') }
    Selector getH2() { newElement('h2') }
    Selector getH3() { newElement('h3') }
    Selector getH4() { newElement('h4') }
    Selector getH5() { newElement('h5') }
    Selector getH6() { newElement('h6') }
    Selector getHeader() { newElement('header') }
    Selector getHgroup() { newElement('hgroup') }
    Selector getHr() { newElement('hr') }
    Selector getHtml() { newElement('html') }
    Selector getI() { newElement('i') }
    Selector getIframe() { newElement('iframe') }
    Selector getImg() { newElement('img') }
    Selector getInput() { newElement('input') }
    Selector getIns() { newElement('ins') }
    Selector getKbd() { newElement('kbd') }
    Selector getKeygen() { newElement('keygen') }
    Selector getLabel() { newElement('label') }
    Selector getLegend() { newElement('legend') }
    Selector getLi() { newElement('li') }
    Selector getMap() { newElement('map') }
    Selector getMark() { newElement('mark') }
    Selector getMenu() { newElement('menu') }
    Selector getMeter() { newElement('meter') }
    Selector getNav() { newElement('nav') }
    Selector getNoscript() { newElement('noscript') }
    Selector getObject() { newElement('object') }
    Selector getOl() { newElement('ol') }
    Selector getOptgroup() { newElement('optgroup') }
    Selector getOption() { newElement('option') }
    Selector getOutput() { newElement('output') }
    Selector getP() { newElement('p') }
    Selector getParam() { newElement('param') }
    Selector getPre() { newElement('pre') }
    Selector getProgress() { newElement('progress') }
    Selector getQ() { newElement('q') }
    Selector getRp() { newElement('rp') }
    Selector getRt() { newElement('rt') }
    Selector getRuby() { newElement('ruby') }
    Selector getS() { newElement('s') }
    Selector getSamp() { newElement('samp') }
    Selector getScript() { newElement('script') }
    Selector getSection() { newElement('section') }
    Selector getSelect() { newElement('select') }
    Selector getSmall() { newElement('small') }
    Selector getSource() { newElement('source') }
    Selector getSpan() { newElement('span') }
    Selector getStrong() { newElement('strong') }
    Selector getStyle() { newElement('style') }
    Selector getSub() { newElement('sub') }
    Selector getSummary() { newElement('summary') }
    Selector getSup() { newElement('sup') }
    Selector getTable() { newElement('table') }
    Selector getTbody() { newElement('tbody') }
    Selector getTd() { newElement('td') }
    Selector getTextarea() { newElement('textarea') }
    Selector getTfoot() { newElement('tfoot') }
    Selector getTh() { newElement('th') }
    Selector getThead() { newElement('thead') }
    Selector getTime() { newElement('time') }
    Selector getTitle() { newElement('title') }
    Selector getTr() { newElement('tr') }
    Selector getTrack() { newElement('track') }
    Selector getU() { newElement('u') }
    Selector getUl() { newElement('ul') }
    Selector getVar() { newElement('var') }
    Selector getVideo() { newElement('video') }
    Selector getWbr() { newElement('wbr') }

    Selector newElement(String name) {
        new Selector(name, currentCss)
    }

    //------------------------------------------------------------------> Underscore
    final Underscore underscore = new Underscore(this)
    Underscore get_() { underscore }

    //---> Pseudo-elements
    /** Pseudo-element ::placeholder. */
    PseudoElement.StyleGroup placeholder(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('placeholder', closure)
    }
    /** Pseudo-element ::after. */
    PseudoElement.StyleGroup after(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('after', closure)
    }
    /** Pseudo-element ::before. */
    PseudoElement.StyleGroup before(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('before', closure)
    }
    /** Pseudo-element ::backdrop. */
    PseudoElement.StyleGroup backdrop(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('backdrop', closure)
    }
    /** Pseudo-element ::cue. */
    PseudoElement.StyleGroup cue(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('cue', closure)
    }
    /** Pseudo-element ::firstLetter. */
    PseudoElement.StyleGroup firstLetter(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('first-letter', closure)
    }
    /** Pseudo-element ::firstLine. */
    PseudoElement.StyleGroup firstLine(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('first-line', closure)
    }
    /** Pseudo-element ::selection. */
    PseudoElement.StyleGroup selection(@DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('selection', closure)
    }
    /** Pseudo-element ::slotted. */
    PseudoElement.StyleGroup slotted(String select,
            @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        withPseudoElement('slotted(' + select + ')', closure)
    }

    //---> Pseudo-classes

    /** Pseudo-class: :active. */
    PseudoClass.StyleGroup active(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('active', closure) }

    /** Pseudo-class: :checked. */
    PseudoClass.StyleGroup checked(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('checked', closure) }

    /** Pseudo-class: :default. */
    PseudoClass.StyleGroup defaultPC(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('default', closure) }

    /** Pseudo-class: :disabled. */
    PseudoClass.StyleGroup disabled(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('disabled', closure) }

    /** Pseudo-class: :empty. */
    PseudoClass.StyleGroup empty(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('empty', closure) }

    /** Pseudo-class: :enabled. */
    PseudoClass.StyleGroup enabled(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('enabled', closure) }

    /** Pseudo-class: :first-child. */
    PseudoClass.StyleGroup firstChild(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('first-child', closure) }

    /** Pseudo-class: :first-of-type. */
    PseudoClass.StyleGroup firstOfType(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('first-of-type', closure) }

    /** Pseudo-class: :focus. */
    PseudoClass.StyleGroup focus(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('focus', closure) }

    /** Pseudo-class: :hover. */
    PseudoClass.StyleGroup hover(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('hover', closure) }

    /** Pseudo-class: :indeterminate. */
    PseudoClass.StyleGroup indeterminate(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('indeterminate', closure) }

    /** Pseudo-class: :in-range. */
    PseudoClass.StyleGroup inRange(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('in-range', closure) }

    /** Pseudo-class: :invalid. */
    PseudoClass.StyleGroup invalid(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('invalid', closure) }

    /** Pseudo-class: :lang. */
    PseudoClass.StyleGroup lang(languageCode, 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { withPseudoClass "lang($languageCode)", clos }

    /** Pseudo-class: :last-child. */
    PseudoClass.StyleGroup lastChild(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('last-child', closure) }

    /** Pseudo-class: :last-of-type. */
    PseudoClass.StyleGroup lastOfType(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('last-of-type', closure) }

    /** Pseudo-class: :link. */
    PseudoClass.StyleGroup linkPseudoClass(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('link', closure) }

    /** Pseudo-class: :not(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure). */
    PseudoClass.StyleGroup not(notStyleGroup, 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { withPseudoClass "not($notStyleGroup)", clos }

    /** Pseudo-class: :nth-child. */
    PseudoClass.StyleGroup nthChild(n, 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass "nth-child($n)", closure }

    /** Pseudo-class: :nth-last-child. */
    PseudoClass.StyleGroup nthLastChild(n, 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { withPseudoClass "nth-last-child($n)", clos }

    /** Pseudo-class: :nth-last-of-type. */
    PseudoClass.StyleGroup nthLastOfType(n, 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure clos) { withPseudoClass "nth-last-of-type($n)", clos }

    /** Pseudo-class: :nth-of-type. */
    PseudoClass.StyleGroup nthOfType(n, 
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass "nth-of-type($n)", closure }

    /** Pseudo-class: "nth-child(odd)". */
    PseudoClass.StyleGroup odd(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass "nth-child(odd)", closure }

    /** Pseudo-class: "nth-child(even)". */
    PseudoClass.StyleGroup even(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass "nth-child(even)", closure }

    /** Pseudo-class: :only-child. */
    PseudoClass.StyleGroup onlyChild(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('only-child', closure) }

    /** Pseudo-class: :only-of-type. */
    PseudoClass.StyleGroup onlyOfType(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('only-of-type', closure) }

    /** Pseudo-class: :optional. */
    PseudoClass.StyleGroup optional(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('optional', closure) }

    /** Pseudo-class: :out-of-range. */
    PseudoClass.StyleGroup outOfRange(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('out-of-range', closure) }

    /** Pseudo-class: :read-only. */
    PseudoClass.StyleGroup readOnly(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('read-only', closure) }

    /** Pseudo-class: :read-write. */
    PseudoClass.StyleGroup readWrite(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('read-write', closure) }

    /** Pseudo-class: :required. */
    PseudoClass.StyleGroup required(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('required', closure) }

    /** Pseudo-class: :root. */
    PseudoClass.StyleGroup root(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('root', closure) }

    /** Pseudo-class: :target. */
    PseudoClass.StyleGroup target(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('target', closure) }

    /** Pseudo-class: :valid. */
    PseudoClass.StyleGroup valid(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('valid', closure) }

    /** Pseudo-class: :visited. */
    PseudoClass.StyleGroup visited(
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) { withPseudoClass('visited', closure) }

    PseudoClass.StyleGroup withPseudoClass(String pseudoClass,
        @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {

        def sg = new PseudoClass.StyleGroup(":$pseudoClass", this.config, currentCss)
        closure.delegate = sg
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure(sg)
        currentCss.add sg
        sg
    }

    PseudoElement.StyleGroup withPseudoElement(String pseudoElement,
            @ClosureParams(value = SimpleType, options = "org.groocss.StyleGroup")
            @DelegatesTo(value = StyleGroup, strategy = Closure.DELEGATE_FIRST) Closure closure) {

        def sg = new PseudoElement.StyleGroup("::$pseudoElement", this.config, currentCss)
        closure.delegate = sg
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure(sg)
        currentCss.add sg
        sg
    }
    
    /** Pseudo-class: :active. */
    PseudoClass getActive() { newPseudoClass('active') }

    /** Pseudo-class: :checked. */
    PseudoClass getChecked() { newPseudoClass('checked') }

    /** Pseudo-class: :default. */
    PseudoClass getDefault() { newPseudoClass('default') }

    /** Pseudo-class: :disabled. */
    PseudoClass getDisabled() { newPseudoClass('disabled') }

    /** Pseudo-class: :empty. */
    PseudoClass getEmpty() { newPseudoClass('empty') }

    /** Pseudo-class: :enabled. */
    PseudoClass getEnabled() { newPseudoClass('enabled') }

    /** Pseudo-class: :first-child. */
    PseudoClass getFirstChild() { newPseudoClass('first-child') }

    /** Pseudo-class: :first-of-type. */
    PseudoClass getFirstOfType() { newPseudoClass('first-of-type') }

    /** Pseudo-class: :focus. */
    PseudoClass getFocus() { newPseudoClass('focus') }

    /** Pseudo-class: :hover. */
    PseudoClass getHover() { newPseudoClass('hover') }

    /** Pseudo-class: :indeterminate. */
    PseudoClass getIndeterminate() { newPseudoClass('indeterminate') }

    /** Pseudo-class: :in-range. */
    PseudoClass getInRange() { newPseudoClass('in-range') }

    /** Pseudo-class: :invalid. */
    PseudoClass getInvalid() { newPseudoClass('invalid') }

    /** Pseudo-class: :lang. */
    PseudoClass lang(languageCode) { newPseudoClass "lang($languageCode)" }

    /** Pseudo-class: :last-child. */
    PseudoClass getLastChild() { newPseudoClass('last-child') }

    /** Pseudo-class: :last-of-type. */
    PseudoClass getLastOfType() { newPseudoClass('last-of-type') }

    /** Pseudo-class: :link. */
    PseudoClass getLink() { newPseudoClass('link') }

    /** Pseudo-class: :not(). */
    PseudoClass not(notStyleGroup) { newPseudoClass "not($notStyleGroup)" }

    /** Pseudo-class: :nth-child. */
    PseudoClass nthChild(n) { newPseudoClass "nth-child($n)" }

    /** Pseudo-class: :nth-child(odd). */
    PseudoClass getOdd() { newPseudoClass "nth-child(odd)" }

    /** Pseudo-class: :nth-child(even). */
    PseudoClass getEven() { newPseudoClass "nth-child(even)" }

    /** Pseudo-class: :nth-last-child. */
    PseudoClass nthLastChild(n) { newPseudoClass "nth-last-child($n)" }

    /** Pseudo-class: :nth-last-of-type. */
    PseudoClass nthLastOfType(n) { newPseudoClass "nth-last-of-type($n)" }

    /** Pseudo-class: :nth-of-type. */
    PseudoClass nthOfType(n) { newPseudoClass "nth-of-type($n)" }

    /** Pseudo-class: :only-child. */
    PseudoClass getOnlyChild() { newPseudoClass('only-child') }

    /** Pseudo-class: :only-of-type. */
    PseudoClass getOnlyOfType() { newPseudoClass('only-of-type') }

    /** Pseudo-class: :optional. */
    PseudoClass getOptional() { newPseudoClass('optional') }

    /** Pseudo-class: :out-of-range. */
    PseudoClass getOutOfRange() { newPseudoClass('out-of-range') }

    /** Pseudo-class: :read-only. */
    PseudoClass getReadOnly() { newPseudoClass('read-only') }

    /** Pseudo-class: :read-write. */
    PseudoClass getReadWrite() { newPseudoClass('read-write') }

    /** Pseudo-class: :required. */
    PseudoClass getRequired() { newPseudoClass('required') }

    /** Pseudo-class: :root. */
    PseudoClass getRoot() { newPseudoClass('root') }

    /** Pseudo-class: :target. */
    PseudoClass getTarget() { newPseudoClass('target') }

    /** Pseudo-class: :valid. */
    PseudoClass getValid() { newPseudoClass('valid') }

    /** Pseudo-class: :visited. */
    PseudoClass getVisited() { newPseudoClass('visited') }

    PseudoClass newPseudoClass(String value) {
        new PseudoClass(value)
    }

    // -----> Pseudo-elements
    /** Pseudo-element ::placeholder. */
    PseudoElement getPlaceholder() { new PseudoElement('placeholder') }

    /** Pseudo-element ::after. */
    PseudoElement getAfter() { new PseudoElement('after') }

    /** Pseudo-element ::before. */
    PseudoElement getBefore() { new PseudoElement('before') }

    /** Pseudo-element ::backdrop. */
    PseudoElement getBackdrop() { new PseudoElement('backdrop') }

    /** Pseudo-element ::cue. */
    PseudoElement getCue() { new PseudoElement('cue') }

    /** Pseudo-element ::firstLetter. */
    PseudoElement getFirstLetter() { new PseudoElement('first-letter') }

    /** Pseudo-element ::firstLine. */
    PseudoElement getFirstLine() { new PseudoElement('first-line') }

    /** Pseudo-element ::selection. */
    PseudoElement getSelection() { new PseudoElement('selection') }

    /** Pseudo-element ::slotted. */
    PseudoElement slotted(String select) { new PseudoElement('slotted(' + select + ')') }

    Raw raw(String raw) {
        def r = new Raw(raw)
        currentCss << r
        r
    }

    /** Adds a comment to be included in output. */
    Comment comment(String comment) {
        def com = new Comment(comment)
        currentCss << com
        com
    }

    /** Imports given Groocss file at filename. */
    MediaCSS importFile(Map params = [:], String filename) { importReader params, new File(filename).newReader() }

    /** Imports given Groocss file. */
    MediaCSS importFile(Map params = [:], File file) { importReader params, file.newReader() }

    /** Imports given Groocss. */
    MediaCSS importString(Map params = [:], String groocss) { importReader(params, new StringReader(groocss)) }

    /** Imports given Groocss input using given Reader. */
    MediaCSS importReader(Map params = [:], Reader reader) {
        def shell = makeShell(true, currentCss.config)
        def script = shell.parse(reader)
        def binding = script.binding
        params.each { binding.setVariable((String) it.key, it.value) }
        script.binding = binding
        script.invokeMethod('setConfig', css.config)
        def result = script.run()
        MediaCSS other = (MediaCSS) script.getProperty('css')
        addIfResultIsGrooCSS(result, other)
        currentCss.add other
    }

    /** Imports given Groocss file using given InputStream. */
    MediaCSS importStream(Map params = [:], InputStream stream) {
        importReader params, new InputStreamReader(stream)
    }

}
