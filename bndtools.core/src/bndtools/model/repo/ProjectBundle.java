package bndtools.model.repo;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

import aQute.bnd.build.Project;
import bndtools.Logger;
import bndtools.api.ILogger;

public class ProjectBundle implements IAdaptable {
    private static final ILogger logger = Logger.getLogger();

    private final Project project;
    private final String bsn;

    ProjectBundle(Project project, String bsn) {
        this.project = project;
        this.bsn = bsn;
    }

    public Project getProject() {
        return project;
    }

    public String getBsn() {
        return bsn;
    }

    @Override
    public String toString() {
        return "ProjectBundle [project=" + project + ", bsn=" + bsn + "]";
    }

    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        Object result = null;

        if (IFile.class.equals(adapter) || IResource.class.equals(adapter)) {
            try {
                File targetDir = project.getTarget();
                File bundleFile = new File(targetDir, bsn + ".jar");
                if (bundleFile.isFile()) {
                    Path path = new Path(bundleFile.getAbsolutePath());
                    result = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
                }
            } catch (Exception e) {
                logger.logError(MessageFormat.format("Error retrieving bundle {0} from project {1}.", bsn, project.getName()), e);
            }
        }
        return result;
    }
}