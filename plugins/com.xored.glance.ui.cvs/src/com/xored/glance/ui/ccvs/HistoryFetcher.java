/*******************************************************************************
 * Copyright (c) 2012 xored software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     xored software, Inc. - initial API and implementation (Ivan Lobachev)
 ******************************************************************************/
package com.xored.glance.ui.ccvs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;

@SuppressWarnings("restriction")
public class HistoryFetcher {
	private static final String HISTORY_PATTERN = "[ARM]\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(\\S*)\\s*(.*)";

	public HistoryFetcher(/* ICVSRemoteFolder parentFolder */) {
	}

	private List<String> downloadHistory(Session session,
			IProgressMonitor progress, ICVSRemoteFolder parentFolder,
			String lastDate) throws CVSException {
		final List<String> res = new ArrayList<String>();
		LocalOption[] options;
		if (lastDate != null && lastDate.length() > 0) {
			options = new LocalOption[] { HistoryCommand.ADDED_REMOVED_ENTRIES,
					HistoryCommand.ALL_USERS,
					HistoryCommand.makeDateOption(lastDate) };
		} else {
			options = new LocalOption[] { HistoryCommand.ADDED_REMOVED_ENTRIES,
					HistoryCommand.ALL_USERS };
		}

		(new HistoryCommand()).execute(session, Command.NO_GLOBAL_OPTIONS,
				options, new ICVSResource[] { parentFolder },
				new ICommandOutputListener() {

					public IStatus errorLine(String line,
							ICVSRepositoryLocation location,
							ICVSFolder commandRoot, IProgressMonitor monitor) {
						// System.out.println(line);
						return OK;
					}

					public IStatus messageLine(String line,
							ICVSRepositoryLocation location,
							ICVSFolder commandRoot, IProgressMonitor monitor) {
						// System.out.println(line);
						if (line.startsWith("A ") || line.startsWith("R ")
								|| line.startsWith("M ")) {
							res.add(line);
						}
						return OK;
					}
				}, Policy.subMonitorFor(progress, 90));
		return res;
	}

	protected List<String> getModules(Session session,
			IProgressMonitor progress, ICVSRemoteFolder parentFolder)
			throws CVSException {
		final List<String> res = new ArrayList<String>();
		// Build the local options
		final List<LocalOption> localOptions = new ArrayList<LocalOption>();
		localOptions.add(Update.RETRIEVE_ABSENT_DIRECTORIES);

		Command.UPDATE.execute(session,
				new GlobalOption[] { Command.DO_NOT_CHANGE },
				localOptions
						.toArray(new LocalOption[localOptions.size()]),
				new ICVSResource[] { parentFolder },
				new ICommandOutputListener() {

					public IStatus errorLine(String line,
							ICVSRepositoryLocation location,
							ICVSFolder commandRoot, IProgressMonitor monitor) {
						// cvs update: New directory `CVSROOT' -- ignored
						String prefix = "cvs update: New directory ";
						String moduleName = line;
						if (line.startsWith(prefix)) {
							moduleName = line.substring(prefix.length() + 1,
									line.lastIndexOf("'"));
							res.add(moduleName);
						}
						return OK;
					}

					public IStatus messageLine(String line,
							ICVSRepositoryLocation location,
							ICVSFolder commandRoot, IProgressMonitor monitor) {
						return OK;
					}
				}, Policy.subMonitorFor(progress, 90));
		return res;
	}

	private CVSHistoryTree createHistory(Session session,
			IProgressMonitor progress, ICVSRemoteFolder parentFolder,
			List<String> entries) throws CVSException {
		final CVSHistoryTree tree = new CVSHistoryTree(parentFolder
				.getRepository(), parentFolder.getTag());

		Pattern p = Pattern.compile(HISTORY_PATTERN);
		CVSTag tag = parentFolder.getTag();
		if (tag == null) {
			tag = new CVSTag();
		}
		for (String entry : entries) {
			Matcher m = p.matcher(entry);
			if (m.find()) {
				if (entry.startsWith("A ")) {
					tree.createFile(m.group(6), tag.getName() + "/"
							+ m.group(7), m.group(5));
				} else if (entry.startsWith("R ")) {
					tree.removeFile(m.group(6), tag.getName() + "/"
							+ m.group(7));
				} else if (entry.startsWith("M ")) {
					tree.updateFileRevision(m.group(6), tag.getName() + "/"
							+ m.group(7), m.group(5));
				}
			}

		}
		return tree;
	}

	public CVSHistoryTree fetchHistory(IProgressMonitor progress,
			ICVSRemoteFolder parentFolder) throws CVSException {
		progress.beginTask(null, 100);
		Session session = new Session(parentFolder.getRepository(),
				parentFolder, false /* output to console */);
		session
				.open(Policy.subMonitorFor(progress, 10), false /* read-only */);
		try {
			String fileName = GlanceCVSPlugin.getDefault().getStateLocation()
					.append(
							parentFolder.getRepository().toString().replace(
									"/", "_").replace(":", "-")).toString();
			Date date = getCacheDate(fileName);

			List<String> cacheEntries = new ArrayList<String>();
			if (date != null) {
				cacheEntries = readHistoryFromFile(fileName);
			}
			List<String> downloadedEntries = downloadHistory(session, progress,
					parentFolder, dateToString(date));
			Date lastDate = getLastEntryDate(downloadedEntries);
			// lastDate.to
			if (lastDate != null) {
				writeCacheDate(fileName, lastDate);
			}
			writeHistoryToFile(fileName, downloadedEntries);
			cacheEntries.addAll(downloadedEntries);
			CVSHistoryTree historyTree = createHistory(session, progress,
					parentFolder, cacheEntries);
			List<String> modulesList = getModules(session, progress,
					parentFolder);

			ICVSHistoryNode[] nodes = historyTree.getChild("HEAD")
					.getNodeChildren();
			for (ICVSHistoryNode node : nodes) {
				if (!modulesList.contains(node.getElementName())) {
					node.removed();
				}
			}

			return historyTree;
		} finally {
			session.close();
		}
	}

	private Date getCacheDate(String fileName) {
		Date res = null;
		try {
			File file = new File(fileName + "_date.txt");
			if (!file.exists()) {
				return null;
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			res = parseDate(reader.readLine());
			res = new Date(res.getTime() + 60000);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	private void writeCacheDate(String fileName, Date date) {
		try {
			File file = new File(fileName + "_date.txt");
			file.createNewFile();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(dateToString(date));
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeHistoryToFile(String fileName, List<String> entries) {
		try {
			RandomAccessFile raf = new RandomAccessFile(fileName + "cont.txt",
					"rw");
			raf.seek(raf.length());
			for (String entry : entries) {
				raf.writeUTF(entry + "\n");
			}
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<String> readHistoryFromFile(String fileName) {
		List<String> res = new ArrayList<String>();
		try {
			RandomAccessFile raf = new RandomAccessFile(fileName + "cont.txt",
					"rw");
			raf.seek(0);
			String line;
			while (raf.getFilePointer() < raf.length()) {
				line = raf.readUTF();
				res.add(line);
			}
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	private Date getLastEntryDate(List<String> entries) {
		String lastDate = "";
		String curDate = "";
		Pattern p = Pattern.compile("[ARM]\\s*(\\S*\\s*\\S*\\s*\\S*)");
		for (String entry : entries) {
			Matcher m = p.matcher(entry);
			if (m.find()) {
				curDate = m.group(1);
				if (lastDate.compareTo(curDate) < 0) {
					lastDate = curDate;
				}
			}
		}
		return parseDate(lastDate);
	}

	private String dateToString(Date date) {
		if (date == null) {
			return null;
		}
		return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")).format(date);
	}

	private Date parseDate(String strDate) {
		ArrayList<SimpleDateFormat> dateFormats = new ArrayList<SimpleDateFormat>();
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm Z"));
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z"));
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd hh:mm"));
		dateFormats.add(new SimpleDateFormat("yyyy MM dd hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy.MM.dd hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"));

		dateFormats.add(new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy MMM dd hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy.MMM.dd hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy/MMM/dd hh:mm:ss"));

		dateFormats.add(new SimpleDateFormat("dd-MM-yyyy hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("dd MM yyyy hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("dd.MM.yyyy hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss"));

		dateFormats.add(new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("dd MMM yyyy hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("dd.MMM.yyyy hh:mm:ss"));

		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
		dateFormats.add(new SimpleDateFormat("yyyy MM dd"));
		dateFormats.add(new SimpleDateFormat("yyyy.MM.dd"));

		dateFormats.add(new SimpleDateFormat("yyyy-MMM-dd"));
		dateFormats.add(new SimpleDateFormat("yyyy MMM dd"));
		dateFormats.add(new SimpleDateFormat("yyyy.MMM.dd"));

		dateFormats.add(new SimpleDateFormat("dd-MM-yyyy"));
		dateFormats.add(new SimpleDateFormat("dd MM yyyy"));
		dateFormats.add(new SimpleDateFormat("dd.MM.yyyy"));

		dateFormats.add(new SimpleDateFormat("dd-MMM-yy"));
		dateFormats.add(new SimpleDateFormat("dd MMM yy"));
		dateFormats.add(new SimpleDateFormat("dd.MMM.yy"));
		dateFormats.add(new SimpleDateFormat("dd/MMM/yy"));

		Date myDate = null;

		for (SimpleDateFormat myFormat : dateFormats) {
			try {
				myDate = myFormat.parse(strDate);
				break;
			} catch (Exception e) {
			}
		}
		return myDate;
	}

}
