package org.jakarta.lsp4e;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.core.resources.IResource;
import java.util.function.Function;
import org.eclipse.core.internal.utils.FileUtil;


import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class SimpleTraverser {
	private static final String JDT_SCHEME = "jdt";
	public static final String PATH_SEPARATOR = "/";
	
	public static void run() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {
			System.out.println("Project Name: " + project.getName());
		}
	}
	
	// Converts uri String to a URI
	public static URI toURI(String uriString) {
		if (uriString == null || uriString.isEmpty()) {
			return null;
		}
		try {
			URI uri = new URI(uriString);
			if (Platform.OS_WIN32.equals(Platform.getOS()) && URIUtil.isFileURI(uri)) {
				uri = URIUtil.toFile(uri).toURI();
			}
			return uri;
		} catch (URISyntaxException e) {
			System.out.println("Failed to resolve "+uriString);
			return null;
		}
	}
	
	// Convert a URI to an IFILE
	public static ICompilationUnit resolveCompilationUnitFromURI(URI uri) {
		if (uri == null || JDT_SCHEME.equals(uri.getScheme()) || !uri.isAbsolute()){
			return null;
		}

		IFile resource = (IFile) findResource(uri, ResourcesPlugin.getWorkspace().getRoot()::findFilesForLocationURI);
		if(resource != null) {
			return resolveCompilationUnit(resource);
		}
		return null;
	}
	
	public static boolean isJavaProj(IProject project) {
		try {
			return project != null && project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			return false;
		}
	}
	
	public static ICompilationUnit resolveCompilationUnit(IFile resource) {
		if(resource != null){
			if(!isJavaProj(resource.getProject())){
				return null;
			}
			if (resource.getFileExtension() != null) {
				String name = resource.getName();
				if (org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(name)) {
					return JavaCore.createCompilationUnitFrom(resource);
				}
			}
		}

		return null;
	}
	
	// Resolve Information from the file
	public static IResource findResource(URI uri, Function<URI, IResource[]> resourceFinder) {
		if (uri == null || !"file".equals(uri.getScheme())) {
			return null;
		}
		IResource[] resources = resourceFinder.apply(uri);
		if (resources.length == 0) {
			//On Mac, Linked resources are referenced via the "real" URI, i.e file://USERS/username/...
			//instead of file://Users/username/..., so we check against that real URI.
			URI realUri = FileUtil.realURI(uri);
			if (!uri.equals(realUri)) {
				uri = realUri;
				resources = resourceFinder.apply(uri);
			}
		}
		if (resources.length == 0 && Platform.OS_WIN32.equals(Platform.getOS()) && uri.toString().startsWith(ResourceUtils.FILE_UNC_PREFIX)) {
			String uriString = uri.toString();
			int index = uriString.indexOf(PATH_SEPARATOR, ResourceUtils.FILE_UNC_PREFIX.length());
			if (index > 0) {
				String server = uriString.substring(ResourceUtils.FILE_UNC_PREFIX.length(), index);
				uriString = uriString.replace(server, server.toUpperCase());
				try {
					uri = new URI(uriString);
				} catch (URISyntaxException e) {
//					JavaLanguageServerPlugin.logException(e.getMessage(), e);
				}
				resources = resourceFinder.apply(uri);
			}
		}
		switch(resources.length) {
		case 0:
			return null;
		case 1:
			return resources[0];
		default://several candidates if a linked resource was created before the real project was configured
				IResource resource = null;
				for (IResource f : resources) {
					System.out.println("Loop REsource");
//				//delete linked resource
//				if (JavaLanguageServerPlugin.getProjectsManager().getDefaultProject().equals(f.getProject())) {
//					try {
//						f.delete(true, null);
//					} catch (CoreException e) {
//							JavaLanguageServerPlugin.logException(e.getMessage(), e);
//					}
//				}
//				//find closest project containing that file, in case of nested projects
//					if (resource == null || f.getProjectRelativePath().segmentCount() < resource.getProjectRelativePath().segmentCount()) {
//						resource = f;
//				}
			}
				return resource;
		}
	}
}
