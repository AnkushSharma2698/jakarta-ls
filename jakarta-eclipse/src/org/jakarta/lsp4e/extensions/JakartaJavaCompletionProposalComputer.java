package org.jakarta.lsp4e.extensions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.lsp4e.operations.completion.LSContentAssistProcessor;

public class JakartaJavaCompletionProposalComputer implements IJavaCompletionProposalComputer {
	private static TimeUnit TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
	private static long TIMEOUT_LENGTH = 2000;
	private LSContentAssistProcessor lsContentAssistProcessor;
	public JakartaJavaCompletionProposalComputer() {
		lsContentAssistProcessor = new LSContentAssistProcessor();
	}
	@Override
	public void sessionStarted() {
	}
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		CompletableFuture<ICompletionProposal[]> future = CompletableFuture.supplyAsync(() -> {
			return lsContentAssistProcessor.computeCompletionProposals(context.getViewer(), context.getInvocationOffset());
		});
		try {
			return Arrays.asList(future.get(TIMEOUT_LENGTH, TIMEOUT_UNIT));
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		IContextInformation[] contextInformation = lsContentAssistProcessor.computeContextInformation(context.getViewer(), context.getInvocationOffset());
		return Arrays.asList(contextInformation);
	}
	@Override
	public String getErrorMessage() {
		return lsContentAssistProcessor.getErrorMessage();
	}
	@Override
	public void sessionEnded() {
		
	}
}
