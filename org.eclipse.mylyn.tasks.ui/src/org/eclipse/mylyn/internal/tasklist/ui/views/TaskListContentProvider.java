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

package org.eclipse.mylar.internal.tasklist.ui.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.internal.tasklist.ITask;
import org.eclipse.mylar.internal.tasklist.ITaskContainer;
import org.eclipse.mylar.internal.tasklist.ITaskListElement;
import org.eclipse.mylar.internal.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.internal.tasklist.Task;
import org.eclipse.mylar.internal.tasklist.TaskCategory;
import org.eclipse.mylar.internal.tasklist.ui.AbstractTaskFilter;
import org.eclipse.swt.widgets.Text;

/**
 * @author Mik Kersten
 */
public class TaskListContentProvider implements IStructuredContentProvider, ITreeContentProvider {

	private final TaskListView view;

	private static class ContentTaskFilter extends AbstractTaskFilter {
		@Override
		public boolean select(Object element) {
			return true;
		}

		@Override
		public boolean shouldAlwaysShow(ITask task) {
			return super.shouldAlwaysShow(task);
		}
	};

	private ContentTaskFilter contentTaskFilter = new ContentTaskFilter();

	public TaskListContentProvider(TaskListView view) {
		this.view = view;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		this.view.expandToActiveTasks();
	}

	public void dispose() {
		// ignore
	}

	public Object[] getElements(Object parent) {
		if (parent.equals(this.view.getViewSite())) {
			return applyFilter(MylarTaskListPlugin.getTaskListManager().getTaskList().getRootElements()).toArray();
		}
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof ITask) {
			if (((ITask) child).getParent() != null) {
				return ((ITask) child).getParent();
			} else {
				return ((ITask) child).getCategory();
			}
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		return getFilteredChildrenFor(parent).toArray();
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof TaskCategory) {
			ITaskContainer cat = (ITaskContainer) parent;
			return cat.getChildren() != null && cat.getChildren().size() > 0;
		} else if (parent instanceof Task) {
			Task t = (Task) parent;
			return t.getChildren() != null && t.getChildren().size() > 0;
		} else if (parent instanceof AbstractRepositoryQuery) {
			AbstractRepositoryQuery t = (AbstractRepositoryQuery) parent;
			return t.getHits() != null && t.getHits().size() > 0;
		}
		return false;
	}

	private List<ITaskListElement> applyFilter(Set<ITaskListElement> roots) {
		String filterText = ((Text) this.view.getFilteredTree().getFilterControl()).getText();
		if (containsNoFilterText(filterText)) {
			List<ITaskListElement> filteredRoots = new ArrayList<ITaskListElement>();
			for (ITaskListElement element : roots) {
//			for (int i = 0; i < list.size(); i++) {
				if (element instanceof ITask) {
					if (!filter(element)) {
						filteredRoots.add(element);
					}
				} else if (element instanceof TaskCategory) {
					if (selectCategory((TaskCategory)element)) {
						filteredRoots.add(element);
					}
				} else if (element instanceof AbstractRepositoryQuery) {
					if (selectQuery((AbstractRepositoryQuery)element)) {
						filteredRoots.add(element);
					}
				}
			}
			return filteredRoots;
		} else {
			return new ArrayList<ITaskListElement>(roots);
		}
	}

	/**
	 * See bug 109693
	 */
	private boolean containsNoFilterText(String filterText) {
		return filterText == null || filterText.length() == 0;
	}

	private boolean selectQuery(AbstractRepositoryQuery cat) {
		Set<? extends ITaskListElement> list = cat.getHits();
		if (list.size() == 0) {
			return true;
		}
		for (ITaskListElement element : list) {
			if (!filter(element)) {
				return true;
			}
		}
//		for (int i = 0; i < list.size(); i++) {
//			if (!filter(list.get(i))) {
//				return true;
//			}
//		}
		return false;
	}

	private boolean selectCategory(ITaskContainer cat) {
		if (cat.isArchive()) {
			for (ITask task : cat.getChildren()) {
				if (contentTaskFilter.shouldAlwaysShow(task)) {
					ITask t = MylarTaskListPlugin.getTaskListManager().getTaskForHandle(task.getHandleIdentifier(),
							false);
					if (t == null)
						return true;
				}
			}
			return false;
		}
		Set<? extends ITaskListElement> list = cat.getChildren();
		if (list.size() == 0) {
			return true;
		}
		for (ITaskListElement element : list) {
			if (!filter(element)) {
				return true;
			}
		}
//		for (int i = 0; i < list.size(); i++) {
//			if (!filter(list.get(i))) {
//				return true;
//			}
//		}
		return false;
	}

	private List<Object> getFilteredChildrenFor(Object parent) {
		if (containsNoFilterText(((Text) this.view.getFilteredTree().getFilterControl()).getText())
				|| ((Text) this.view.getFilteredTree().getFilterControl()).getText().startsWith(TaskListView.FILTER_LABEL)) {
			List<Object> children = new ArrayList<Object>();
			if (parent instanceof TaskCategory) { 
				if (((ITaskContainer) parent).isArchive()) {
					for (ITask task : ((ITaskContainer) parent).getChildren()) {
						if (contentTaskFilter.shouldAlwaysShow(task)) {
							ITask t = MylarTaskListPlugin.getTaskListManager().getTaskForHandle(
									task.getHandleIdentifier(), false);
							if (t == null)
								children.add(task);
						}
					}
					return children;
				}
				Set<? extends ITaskListElement> list = ((ITaskContainer) parent).getChildren();
				for (ITaskListElement element : list) {
					if (!filter(element)) {
						children.add(element);
					}
				}
//				for (int i = 0; i < list.size(); i++) {
//					if (!filter(list.get(i))) {
//						children.add(list.get(i));
//					}
//				}
				return children;
			} else if (parent instanceof AbstractRepositoryQuery) {
				Set<? extends ITaskListElement> list = ((AbstractRepositoryQuery) parent).getHits();
				for (ITaskListElement element : list) {
					if (!filter(element)) {
						children.add(element);
					}
				}
//				for (int i = 0; i < list.size(); i++) {
//					if (!filter(list.get(i))) {
//						children.add(list.get(i));
//					}
//				}
				return children;
			} else if (parent instanceof Task) {
				Set<ITask> subTasks = ((Task) parent).getChildren();
				for (ITask t : subTasks) {
					if (!filter(t)) {
						children.add(t);
					}
				}
				return children;
			}
		} else {
			List<Object> children = new ArrayList<Object>();
			if (parent instanceof TaskCategory) {
				children.addAll(((ITaskContainer) parent).getChildren());
				return children;
			} else if (parent instanceof AbstractRepositoryQuery) {
				children.addAll(((AbstractRepositoryQuery) parent).getHits());
				return children;
			} else if (parent instanceof Task) {
				children.addAll(((Task) parent).getChildren());
				return children;
			}
		}
		return new ArrayList<Object>();
	}

	private boolean filter(Object obj) {
		for (AbstractTaskFilter filter : this.view.filters) {
			if (!filter.select(obj)) {
				return true;
			}
		}
		return false;
	}
}
