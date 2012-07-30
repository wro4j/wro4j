package com.google.javascript.jscomp;



/**
 * Created to handle the thread safety issue of the {@link ComposeWarningsGuard} which hopefully will be solved in the
 * future.
 * <p/>
 * You can track the <a href="http://code.google.com/p/closure-compiler/issues/detail?id=781">issue</a> on google code.
 */
public class ThreadSafeComposeWarningsGuard
    extends ComposeWarningsGuard {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean enables(final DiagnosticGroup group) {
    synchronized (this) {
      return super.enables(group);
    }
  }
  
  @Override
  void addGuard(final WarningsGuard guard) {
    synchronized (this) {
      super.addGuard(guard);
    }
  }
  
  @Override
  public boolean disables(final DiagnosticGroup group) {
    synchronized (this) {
      return super.disables(group);
    }
  }
}
