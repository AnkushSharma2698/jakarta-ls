package org.jakarta.lsp4e;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;

import io.microshed.jakartals.api.JakartaLanguageClientAPI;
import io.microshed.jakartals.commons.JakartaDiagnosticsParams;

import org.jakarta.lsp4e.SimpleTraverser;



public class JakartaLanguageClient extends LanguageClientImpl implements JakartaLanguageClientAPI {
    
    public JakartaLanguageClient() {
        // do nothing
    }
    
    private IProgressMonitor getProgressMonitor(CancelChecker cancelChecker) {
        IProgressMonitor monitor = new NullProgressMonitor() {
            public boolean isCanceled() {
                cancelChecker.checkCanceled();
                return false;
            };
        };
        return monitor;
    }
    
    @Override
    public CompletableFuture<Hover> getJavaHover() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<PublishDiagnosticsParams>> getJavaDiagnostics(
            JakartaDiagnosticsParams javaParams) {
        Activator.log(new Status(IStatus.INFO, "diagnostic request received", "diagnostic request receieved"));
        // creating a test diagnostic
        return CompletableFutures.computeAsync((cancelChecker) -> {
            IProgressMonitor monitor = getProgressMonitor(cancelChecker);
                List<PublishDiagnosticsParams> publishDiagnostics = new ArrayList<PublishDiagnosticsParams>();
                List<String> uris = javaParams.getUris();
                for (String uri : uris) {
                	
                	URI u = SimpleTraverser.toURI(uri);
                	ICompilationUnit unit = SimpleTraverser.resolveCompilationUnitFromURI(u);
                	if (unit != null) {
                		System.out.println("--class name: " + unit.getElementName());
                    	IType[] alltypes;
    					try {
    						alltypes = unit.getAllTypes();
    						for (IType type: alltypes)  {
    	                		IMethod[] methods = type.getMethods();
    	                		for (IMethod method : methods) {
    								System.out.println("--Method name: "+ method.getElementName());
    								System.out.println("Signature: "+ method.getSignature());
    								System.out.println("Return Type: "+ method.getReturnType());
    								System.out.println("source: "+ method.getSource());
    								System.out.println("to string: "+ method.toString());
    								System.out.println("new: "+ method.getPath().toString());
    							}
    	                	}
    					} catch (JavaModelException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
                	}
                    List<Diagnostic> diagnostics = new ArrayList<>();
                    Range diagRange = new Range(new Position(2, 1), new Position(2, 15));
                    diagnostics.add(new Diagnostic(diagRange, "There is some issue over here"));
                    PublishDiagnosticsParams publishDiagnostic = new PublishDiagnosticsParams(uri, diagnostics);
                    publishDiagnostics.add(publishDiagnostic);
                }
                return publishDiagnostics;
        });
    }
}