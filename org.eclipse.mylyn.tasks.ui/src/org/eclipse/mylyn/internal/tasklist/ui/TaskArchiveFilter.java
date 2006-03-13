/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.internal.tasklist.ui;

import org.eclipse.mylar.provisional.tasklist.AbstractTaskContainer;

/**
 * @author Mik Kersten
 */
public class TaskArchiveFilter extends AbstractTaskFilter {

	public boolean select(Object element) {
		if (element instanceof AbstractTaskContainer) {
			AbstractTaskContainer container = (AbstractTaskContainer)element;
			return !container.isArchive();
		} 
		return true;
	}
}
