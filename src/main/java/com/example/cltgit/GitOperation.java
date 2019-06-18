package com.example.cltgit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitOperation {

	//String url = "https://github.com/miyazakisoft/Putu.git";

	public void perform() {
		// this.gitClone();
		// this.gPull();
	}

	public void gitClone(String accountName, String repositoryName) {
		System.out.println("テストB");
		try {
			Repository localRepo = new FileRepository("./" + repositoryName + "/" + Constants.DOT_GIT);
			Git git = new Git(localRepo);

			if (git != null) {
				// . git clone
				git.cloneRepository()
						.setURI("https://github.com/" + accountName + "/" + repositoryName + Constants.DOT_GIT)
						.setDirectory(new File("./git_project/" + accountName + "/" + repositoryName)).call();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void gPull() {
		// git add command and commit command
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		Repository repository = null;
		try {
			repository = repositoryBuilder.setGitDir(new File("./git_project/Putu/" + Constants.DOT_GIT))
					.readEnvironment().findGitDir().setMustExist(true).build();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Git git = new Git(repository);
		if (git != null) {
			// . git pull
			PullCommand pc = git.pull();
			try {
				pc.call();
			} catch (GitAPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
