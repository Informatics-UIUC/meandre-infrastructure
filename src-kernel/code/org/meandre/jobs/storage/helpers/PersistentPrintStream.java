package org.meandre.jobs.storage.helpers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.meandre.core.logger.KernelLoggerFactory;
import org.meandre.jobs.storage.backend.JobInformationBackendAdapter;

// Note: need to use a wrapped PrintStream because the methods of PrintStream call each other and we get duplicated stuff in the console
// For example, I think that calling PrintStream.print(object) translates to PrintStream.print(object.toString()) i.e. print(Object) calls print(String)
// Other methods may cascade as well. Using a wrapped PrintStream solves the problem.

/**
 *
 * @author Boris Capitanu
 *
 */
public class PersistentPrintStream extends PrintStream {

	/** The main logger to use */
	private static final Logger Log = KernelLoggerFactory.getCoreLogger();

	/** The job information backend object */
	private final JobInformationBackendAdapter _joba;

	/** The job id associated with this persistent print stream */
	private final String _jid;

	/** The concurrent queue to send to the persistent storage */
	private final ConcurrentLinkedQueue<Object> _queue = new ConcurrentLinkedQueue<Object>();

	private final AsynchronousUpdateThread _updaterThread;

	private final PrintStream _ps;
	private boolean _error = false;


	public PersistentPrintStream(OutputStream out, JobInformationBackendAdapter joba, String jid) {
		this(out, false, joba, jid);
	}

	public PersistentPrintStream(OutputStream out, boolean autoFlush, JobInformationBackendAdapter joba, String jid) {
		super(new ByteArrayOutputStream()); // redirect to null since we're using a wrapped printstream
		_ps = new PrintStream(out, autoFlush);

		_joba = joba;
		_jid = jid;
		_updaterThread = new AsynchronousUpdateThread();

		Thread thread = new Thread(_updaterThread);
		thread.setName("PersistentPrintStream for job: " + jid);
		thread.start();
	}

	public PersistentPrintStream(OutputStream out, boolean autoFlush, String encoding, JobInformationBackendAdapter joba, String jid)
			throws UnsupportedEncodingException {
		super(new ByteArrayOutputStream()); // redirect to null since we're using a wrapped printstream
		_ps = new PrintStream(out, autoFlush, encoding);

		_joba = joba;
		_jid = jid;
		_updaterThread = new AsynchronousUpdateThread();

		Thread thread = new Thread(_updaterThread);
		thread.setName("PersistentPrintStream for job: " + jid);
		thread.start();
	}

	private void enqueue(Object o) {
		enqueue(o, false);
	}

	private void enqueue(Object o, boolean eol) {
		_queue.offer(o);
		if (eol)
			_queue.offer('\n');

		_updaterThread.wakeUp();
	}

	@Override
	public PrintStream append(char c) {
		_ps.append(c);
		enqueue(c);
		return this;
	}

	@Override
	public PrintStream append(CharSequence csq) {
		_ps.append(csq);
		enqueue(csq);
		return this;
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		_ps.append(csq, start, end);
		enqueue(csq.subSequence(start,  end));
		return this;
	}

	@Override
	public void print(boolean b) {
		_ps.print(b);
		enqueue(b);
	}

	@Override
	public void print(char c) {
		_ps.print(c);
		enqueue(c);
	}

	@Override
	public void print(char[] s) {
		_ps.print(s);
		enqueue(s);
	}

	@Override
	public void print(double d) {
		_ps.print(d);
		enqueue(d);
	}

	@Override
	public void print(float f) {
		_ps.print(f);
		enqueue(f);
	}

	@Override
	public void print(int i) {
		_ps.print(i);
		enqueue(i);
	}

	@Override
	public void print(long l) {
		_ps.print(l);
		enqueue(l);
	}

	@Override
	public void print(Object obj) {
		_ps.print(obj);
		enqueue(obj);
	}

	@Override
	public void print(String s) {
		_ps.print(s);
		enqueue(s);
	}

	@Override
	public void println() {
		_ps.println();
		enqueue("", true);
	}

	@Override
	public void println(boolean x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void println(char x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void println(char[] x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void println(double x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void println(float x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void println(int x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void println(long x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void println(Object x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void println(String x) {
		_ps.println(x);
		enqueue(x, true);
	}

	@Override
	public void write(byte[] b) throws IOException {
		_ps.write(b);
		enqueue(new String(b));
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		_ps.write(buf, off, len);
		enqueue(new String(buf, off, len));
	}

	@Override
	public void write(int b) {
		_ps.write(b);
		enqueue(Character.toChars(b));
	}

	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		_ps.format(l, format, args);
		enqueue(String.format(l, format, args));
		return this;
	}

	@Override
	public PrintStream format(String format, Object... args) {
		_ps.format(format, args);
		enqueue(String.format(format, args));
		return this;
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		_ps.printf(l, format, args);
		enqueue(String.format(l, format, args));
		return this;
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		_ps.printf(format, args);
		enqueue(String.format(format, args));
		return this;
	}

	@Override
	public void flush() {
		_ps.flush();
	}

	@Override
	protected void setError() {
		_error = true;
	}

	@Override
	public boolean checkError() {
		return _ps.checkError() || _error;
	}

	@Override
	protected void clearError() {
		_error = false;
	}

	@Override
	public void close() {
		_ps.close();
		_updaterThread.setDone();
		super.close(); // close the fake stream as well
	}


	class AsynchronousUpdateThread implements Runnable {

		private final AutoResetEvent _lock = new AutoResetEvent(false);
		private boolean _done = false;

		@Override
		public void run() {
			waitOne();

			do {
				waitOne();

				StringBuilder sb = new StringBuilder();
				Object tmp;
				while ((tmp = _queue.poll()) != null)
					sb.append(tmp);

				if (sb.length() > 0)
					_joba.print(_jid, sb.toString());

			} while (!_queue.isEmpty() || !_done);
		}

		private void waitOne() {
			try {
				_lock.waitOne();
			}
			catch (InterruptedException e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));
				Log.warning(String.format("Error in %s - message: %s%nStack trace: %s", getClass().getName(), e.getMessage(), baos.toString()));
			}
		}

		public void wakeUp() {
			_lock.set();
		}

		public void setDone() {
			_done = true;
			wakeUp();
		}
	}

	class AutoResetEvent {
		private final Object _monitor = new Object();
		private volatile boolean _state = false;

		public AutoResetEvent(boolean state) {
			_state = state;
		}

		public void waitOne() throws InterruptedException {
			synchronized (_monitor) {
				while (_state == false)
					_monitor.wait();

				reset();
			}
		}

		public void set() {
			synchronized (_monitor) {
				_state = true;
				_monitor.notifyAll();
			}
		}

		public void reset() {
			_state = false;
		}
	}
}
