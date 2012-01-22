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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Session;

@SuppressWarnings("restriction")
public class HistoryCommand extends Command {

	public static class MyLocalOption extends LocalOption {
		protected MyLocalOption(String option) {
			super(option, null);
		}

		protected MyLocalOption(String option, String argument) {
			super(option, argument);
		}

		@Override
		public void send(Session session) throws CVSException {
			session.sendArgument(option);
			if (argument != null)
				session.sendArgument(argument);
		}

		public MyLocalOption[] addTo(MyLocalOption[] options) {
			if (this.isElementOf(options)) {
				return options;
			}
			MyLocalOption[] newOptions = new MyLocalOption[options.length + 1];
			System.arraycopy(options, 0, newOptions, 0, options.length);
			newOptions[options.length] = this;
			return newOptions;
		}

		public MyLocalOption[] removeFrom(MyLocalOption[] options) {
			if (!this.isElementOf(options)) {
				return options;
			}
			List<MyLocalOption> result = new ArrayList<MyLocalOption>();
			for (int i = 0; i < options.length; i++) {
				MyLocalOption option = options[i];
				if (!option.equals(this)) {
					result.add(option);
				}
			}
			return result.toArray(new MyLocalOption[result.size()]);
		}
	}

	/*** Local options: specific to log ***/

	public static MyLocalOption makeRevisionOption(String revision) {
		return new MyLocalOption("-r" + revision, null); //$NON-NLS-1$
	}

	public static MyLocalOption makeDateOption(String date) {
		return new MyLocalOption("-D", date); //$NON-NLS-1$
	}

	public static final MyLocalOption ADDED_REMOVED_ENTRIES = new MyLocalOption(
			"-x", "AR"); //$NON-NLS-1$
	public static final MyLocalOption ALL_BRANCHES = new MyLocalOption("-T"); //$NON-NLS-1$
	public static final MyLocalOption ALL_USERS = new MyLocalOption("-a"); //$NON-NLS-1$

	public static final MyLocalOption ALL_ENTRIES = new MyLocalOption("-e"); //$NON-NLS-1$

	public static final MyLocalOption NO_TAGS = new MyLocalOption("-N"); //$NON-NLS-1$

	@Override
	protected String getRequestId() {
		return "history"; //$NON-NLS-1$
	}

	@Override
	protected ICVSResource[] sendLocalResourceState(Session session,
			GlobalOption[] globalOptions, LocalOption[] localOptions,
			ICVSResource[] resources, IProgressMonitor monitor)
			throws CVSException {
		return null;
	}
}
