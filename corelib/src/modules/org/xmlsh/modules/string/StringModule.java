package org.xmlsh.modules.string;

import java.util.List;

import net.sf.saxon.trans.XPathException;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.PackageModule;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.jayway.jsonpath.internal.Utils;

@org.xmlsh.annotations.Module(name = "string")
public class StringModule extends PackageModule {

	@Function(name = "new")
	public static class _new extends AbstractBuiltinFunction {

		@Override
		public XValue run(Shell shell, List<XValue> args) throws Exception {

			XValue obj = XValue.newXValue(Utils.join(" ", args));
			return obj;

		}

	}

	@Function(name = "concat")
	public static class concat extends AbstractBuiltinFunction {

		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {
			return XValue
					.newXValue(Util.stringJoin(Util.toStringList(args), ""));
		}

	}

	@Function(name = "contains")
	public static class contains extends AbstractBuiltinFunction {

		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			switch (args.size()) {
			case 1:
				return XValue.newXValue(false);
			case 2:
				String str = args.get(0).toString();
				String search = args.get(1).toString();
				return XValue.newXValue(str.contains(search));
			default:
				throw new InvalidArgumentException(
						"usage  contains(string expr)");
			}
		}

	}
	@Function(name = "isBlank")
	public static class endsWith extends AbstractBuiltinFunction {

		// tokenize( string regex )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			if (args.size() != 2)
				throw new InvalidArgumentException(
						"usage  endsWith(string expr)");
			String str = args.get(0).toString();
			String search = args.get(1).toString();
			return XValue.newXValue(str.endsWith(search));
		}

	}
	@Function(name = "isBlank")

	public static class isBlank extends AbstractBuiltinFunction {

		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {
			String s = args.size() > 0 ? args.get(0).toString() : null;
			return XValue.newXValue(Util.isBlank(s));
		}

	}
	@Function(name = "isEmpty")

	public static class isEmpty extends AbstractBuiltinFunction {
		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {
			String s = args.size() > 0 ? args.get(0).toString() : null;
			return XValue.newXValue(Util.isEmpty(s));
		}

	}
	@Function(name = "is-equal", names={"equals"})

	public static class isEqual extends AbstractBuiltinFunction {

		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			if (args.size() != 2)
				return XValue.newXValue(false);
			return XValue.newXValue(Util.isEqual(args.get(0).toString(), args
					.get(1).toString()));
		}

	}
	@Function(name = "is-one-of", names={"isOneOf"})
	public static class isOneOf extends AbstractBuiltinFunction {

		public isOneOf() {
			super("is-one-of");
		}

		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			if (args.size() < 2)
				return XValue.newXValue(false);

			String str = args.remove(0).toString();
			for (XValue a : args) {
				if (Util.isEqual(str, a.toString()))
					return XValue.newXValue(true);
			}
			return XValue.newXValue(false);
		}

	}
	@Function(name = "join")

	public static class join extends AbstractBuiltinFunction {

		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			if (args.size() < 2)
				return XValue.newXValue("");

			String sep = args.remove(0).toString();
			return XValue.newXValue(Util.stringJoin(Util.toStringList(args),
					sep));
		}

	}
	@Function(name = "length")

	public static class length extends AbstractBuiltinFunction {
		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {
			return XValue.newXValue(args.size() > 0 ? args.get(0).toString()
					.length() : 0);
		}

	}
	@Function(name = "matches")

	public static class matches extends AbstractBuiltinFunction {

		// tokenize( string regex )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			if (args.size() != 2)
				throw new InvalidArgumentException(
						"usage  matches(string expr)");
			String str = args.get(0).toString();
			String search = args.get(1).toString();
			return XValue.newXValue(str.matches(search));
		}

	}
	@Function(name = "replace",names={ "replace-all"})
	public static class replace extends AbstractBuiltinFunction {

		// tokenize( string regex )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			if (args.size() < 3)
				throw new InvalidArgumentException(
						"usage: replace(string search replace");
			String str = args.get(0).toString();
			String search = args.get(1).toString();
			String replace = args.get(2).toString();

			return XValue.newXValue(str.replaceAll(search, replace));
		}

	}
	@Function(name = "starts-with",names={ "startsWith"})

	public static class startsWith extends AbstractBuiltinFunction {

		// tokenize( string regex )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			if (args.size() != 2)
				throw new InvalidArgumentException(
						"usage  startsWith(string str)");
			String str = args.get(0).toString();
			String search = args.get(1).toString();
			return XValue.newXValue(str.startsWith(search));
		}

	}
	@Function(name = "substring",names={ "substr"})

	public static class substring extends AbstractBuiltinFunction {

		// string:substring( string [start [end] )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {
			switch (args.size()) {
			case 0:
				return XValue.newXValue("");
			case 1:
				return args.get(0);
			case 2:
				return XValue.newXValue(args.get(0).toString()
						.substring(args.get(1).toInt()));
			case 3:
				return XValue.newXValue(args.get(0).toString()
						.substring(args.get(1).toInt(), args.get(2).toInt()));
			default:
				throw new InvalidArgumentException(
						"usage: substring( string [start [end]] ");
			}
		}

	}
	@Function(name = "substring-after")
	public static class substringAfter extends AbstractBuiltinFunction {


		// string:substring-after( string [sub] )
		// using XQuery rules -
		// if sub is empty then return string
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {
			switch (args.size()) {
			case 0:
				return XValue.newXValue("");
			case 1:
				return args.get(0);
			case 2:
				String s = args.get(0).toString();
				String sub = args.get(1).toString();
				if (!sub.isEmpty()) {
					int pos = s.lastIndexOf(sub);
					if (pos < 0) // not found
						s = "";
					else if (pos != s.length())
						s = s.substring(pos + sub.length());
				}
				return XValue.newXValue(s);
			default:
				throw new InvalidArgumentException(
						"usage: substring-after( string substr)");
			}
		}

	}
	@Function(name = "substring-before")

	public static class substringBefore extends AbstractBuiltinFunction {

		// string:substring( string [start [end] )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {
			switch (args.size()) {
			case 0:
				return XValue.newXValue("");
			case 2:
				String s = args.get(0).toString();
				String sub = args.get(1).toString();
				int pos = s.indexOf(sub);
				if (pos <= 0)
					s = "";
				else
					s = s.substring(0, pos);
				return XValue.newXValue(s);
			default:
				throw new InvalidArgumentException(
						"usage: substring-after( string substr)");
			}
		}

	}

	@Function
	public static class tokenize extends AbstractBuiltinFunction {

		// tokenize( string regex )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {

			if (args.size() < 2)
				throw new InvalidArgumentException(
						"usage: tokenize(string [expr])");
			String str = args.get(0).toString();
			String tok = args.size() > 0 ? args.get(1).toString() : "[ \t\r\n]";
			int limit = args.size() > 1 ? args.get(2).toInt() : 0;
			return XValue.newXValue(str.split(tok, limit));
		}

	}

	@Function
	public static class trim extends AbstractBuiltinFunction {


		// string:join( sep , arg ... )
		@Override
		public XValue run(Shell shell, List<XValue> args)
				throws UnexpectedException, XPathException,
				InvalidArgumentException {
			return XValue.newXValue(Util
					.stringJoin(Util.toStringList(args), "").trim());
		}

	}

	
	public StringModule(ModuleConfig config, XClassLoader loader)
			throws CoreException {
		super(config, loader);
		// TODO Auto-generated constructor stub
	}
}
