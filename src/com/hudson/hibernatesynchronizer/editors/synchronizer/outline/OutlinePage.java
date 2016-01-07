package com.hudson.hibernatesynchronizer.editors.synchronizer.outline;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.hudson.hibernatesynchronizer.Plugin;
import com.hudson.hibernatesynchronizer.editors.synchronizer.Editor;
import com.hudson.hibernatesynchronizer.mapping.BaseElement;
import com.hudson.hibernatesynchronizer.mapping.HibernateClassCollectionProperty;
import com.hudson.hibernatesynchronizer.mapping.HibernateClassProperty;
import com.hudson.hibernatesynchronizer.mapping.HibernateDocument;
import com.hudson.hibernatesynchronizer.util.HSUtil;

public class OutlinePage extends ContentOutlinePage {
	private Editor editor;
	private ContentProvider contentProvider;
	private HibernateDocument document;
	
	public OutlinePage (Editor editor) {
		this.editor = editor;
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		if (Plugin.ALLOWED_STARTUP_TIME <= System.currentTimeMillis()) {
			// there is a weird bug if files are open when Eclipse starts up and I think it has to do
			// with the outline page for some reason
			TreeViewer viewer = getTreeViewer();
			// viewer.addDoubleClickListener(new HibernateOutlineDoubleClickListener(this));
			contentProvider = new ContentProvider(this);
			viewer.setContentProvider(contentProvider);
			viewer.setLabelProvider(new LabelProvider());
			document = editor.getDocument();
			if (null != document) {
				try {
					viewer.setInput(document);
					if (document.getClasses().size() == 1) {
						viewer.expandToLevel(3);
					}
					else if (document.getClasses().size() > 1) {
						viewer.expandToLevel(2);
					}
				}
				catch (Exception e) {
					Plugin.getDefault().trace(e);
				}
			}
		}
	}

	public Editor getEditor() {
		return editor;
	}

	public ContentProvider getContentProvider() {
		return contentProvider;
	}
	
	public void refresh (HibernateDocument document) {
		this.document = document;
		refresh();
	}

	public void refresh () {
		if (null != document && null != getTreeViewer()) {
			getTreeViewer().setInput(document);
			if (document.getClasses().size() == 1) {
				getTreeViewer().expandToLevel(2);
			}
		}
	}

	public void makeContributions(
		IMenuManager menuManager,
		IToolBarManager toolBarManager,
		IStatusLineManager statusLineManager) {
		
		MenuManager menuMgr = new MenuManager();
//		menuMgr.add(new AddPropertyActionContribution(this));
//		menuMgr.add(new AddOneToOneActionContribution(this));
//		menuMgr.add(new AddManyToOneActionContribution(this));
		menuMgr.add(new Separator());
//		menuMgr.add(new ToggleNullableActionContribution(this));
//		menuMgr.add(new DeleteNodeActionContribution(this));
		Menu menu = menuMgr.createContextMenu(getTreeViewer().getTree());
		getTreeViewer().getTree().setMenu(menu);
		super.makeContributions(menuManager, toolBarManager, statusLineManager);
	}

	public void setActionBars(IActionBars actionBars) {
		actionBars.getToolBarManager().add(new ShowPropertyAction(this));
		actionBars.getToolBarManager().add(new ShowOneToOneAction(this));
		actionBars.getToolBarManager().add(new ShowManyToOneAction(this));
		actionBars.getToolBarManager().add(new ShowComponentAction(this));
		actionBars.getToolBarManager().add(new ShowCollectionAction(this));
		actionBars.getToolBarManager().add(new ShowQueryAction(this));
		// actionBars.getToolBarManager().add(new ShowRequiredAction(this));
		super.setActionBars(actionBars);
	}

	public class AddPropertyActionContribution extends ActionContributionItem {
		public AddPropertyActionContribution (OutlinePage outlinePage) {
			super(new AddPropertyNodeAction(outlinePage));
		}
	}

	public class AddPropertyNodeAction extends AddNodeAction {

		public AddPropertyNodeAction (OutlinePage outlinePage) {
			super(outlinePage);
		}

		public boolean isForeignKey () {
			return false;
		}
		
		protected String getNodeName() {
			return "property";
		}

		protected String[] getAttributes() {
			return new String[] {"type", "not-null", "length", "update", "insert", "formula", "access", "unique"};
		}

		public String getText() {
			return "Add property Node";
		}
	}

	public class AddOneToOneActionContribution extends ActionContributionItem {
		public AddOneToOneActionContribution (OutlinePage outlinePage) {
			super(new AddOneToOneNodeAction(outlinePage));
		}
	}

	public class AddOneToOneNodeAction extends AddNodeAction {

		public AddOneToOneNodeAction (OutlinePage outlinePage) {
			super(outlinePage);
		}

		public boolean isForeignKey () {
			return true;
		}

		protected String getNodeName() {
			return "one-to-one";
		}

		protected String[] getAttributes() {
			return new String[] {"class", "not-null", "cascade", "outer-join", "property-ref", "access"};
		}

		public String getText() {
			return "Add one-to-one Node";
		}
	}

	public class AddManyToOneActionContribution extends ActionContributionItem {
		public AddManyToOneActionContribution (OutlinePage outlinePage) {
			super(new AddManyToOneNodeAction(outlinePage));
		}
	}

	public class AddManyToOneNodeAction extends AddNodeAction {

		public AddManyToOneNodeAction (OutlinePage outlinePage) {
			super(outlinePage);
		}

		protected String getNodeName() {
			return "many-to-one";
		}

		public boolean isForeignKey () {
			return true;
		}
		
		protected String[] getAttributes() {
			return new String[] {"class", "not-null", "cascade", "outer-join", "update", "insert", "property-ref", "access", "unique"};
		}
		
		public String getText() {
			return "Add many-to-one Node";
		}
	}

	public abstract class AddNodeAction extends Action {
		private OutlinePage outlinePage;
		public AddNodeAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setEnabled(true);
		}

		protected abstract String getNodeName ();
		protected abstract String[] getAttributes ();
		protected abstract boolean isForeignKey ();

		public void runWithEvent(Event event) {
			if (getEditor().isDirty()) {
				HSUtil.showError("You must save the document before performing this operation.", new Shell());
				return;
			}
			
//			AddPropertyDialog apd = new AddPropertyDialog(new Shell(), editor, getNodeName(), getAttributes(), isForeignKey());
//			if (Dialog.OK == apd.open()) {
//				Properties props = apd.getProperties();
//				
//				Node classNode = null;
//				if (document.getClasses().size() == 1) {
//					classNode = ((HibernateClass) document.getClasses().get(0)).getNode();
//				}
//				else {
//					Tree tree = getTreeViewer().getTree();
//					TreeItem[] items = tree.getSelection();
//					for (int i=0; i<items.length; i++) {
//						if (items[i].getData() instanceof BaseElement) {
//							classNode = ((BaseElement) items[i].getData()).getParentRoot().getNode();
//						}
//					}
//				}
//				if (null == classNode) {
//					HSUtil.showError("The class node could not be determined", new Shell());
//					return;
//				}
//				Element propNode = classNode.getOwnerDocument().createElement(getNodeName());
//				for (Iterator i=props.entrySet().iterator(); i.hasNext(); ) {
//					Map.Entry entry = (Map.Entry) i.next();
//					propNode.setAttribute((String) entry.getKey(), (String) entry.getValue());
//				}
//				NodeList propNodes = ((Element) classNode).getElementsByTagName(getNodeName());
//				if (propNodes.getLength() > 0) {
//					Node n = propNodes.item(propNodes.getLength() - 1);
//					if (null != n.getNextSibling()) n = n.getNextSibling();
//					classNode.insertBefore(propNode, n);
//				}
//				else {
//					classNode.appendChild(propNode);
//				}
//	
//				try {
//					IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
//					StringWriter sw = new StringWriter();
//					XMLHelper xmlHelper = new XMLHelper(new PrintWriter(sw));
//					xmlHelper.printTree(classNode.getOwnerDocument(), contentProvider.getDocument().getParser(), file.getContents());
//					((XMLDocumentProvider) getEditor().getDocumentProvider()).setContent(editor.getEditorInput(), new ByteArrayInputStream(sw.toString().getBytes()));
//					HibernateDocument hd = Synchronizer.getClasses((IFileEditorInput) editor.getEditorInput());
//					Synchronizer.synchronize((IFileEditorInput) editor.getEditorInput(), editor.getHibernateSourceViewer(), Display.getCurrent().getActiveShell()); 
//					refresh(hd);
//				}
//				catch (CoreException e) {
//					HSUtil.showError(e.getMessage(), new Shell());
//				}
//			}
		}
	}

	public class DeleteNodeActionContribution extends ActionContributionItem {
		public DeleteNodeActionContribution (OutlinePage outlinePage) {
			super(new DeleteNodeAction(outlinePage));
		}
	}
	
	public class DeleteNodeAction extends Action {
		private OutlinePage outlinePage;
		public DeleteNodeAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setText("Delete Node");
			this.setEnabled(true);
		}

		public void runWithEvent(Event event) {
			boolean rtn = MessageDialog.openConfirm(new Shell(), "Node deletion confirmation", "Are you sure you want to delete this node?");
			if (rtn) {
				if (getEditor().isDirty()) {
					HSUtil.showError("You must save the document before performing this operation.", new Shell());
					return;
				}
				Tree tree = getTreeViewer().getTree();
				TreeItem[] items = tree.getSelection();
				Document doc = null;
				for (int i=0; i<items.length; i++) {
					TreeItem item = items[i];
					if (item.getData() instanceof BaseElement) {
						Node node = ((BaseElement) item.getData()).getNode();
						if (null != node) {
							doc = node.getOwnerDocument();
							node.getParentNode().removeChild(node);
						}
					}
				}
//				try {
//					IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
//					editor.doSave(null);
//					StringWriter sw = new StringWriter();
//					XMLHelper xmlHelper = new XMLHelper(new PrintWriter(sw));
//					xmlHelper.printTree(doc, contentProvider.getDocument().getParser(), file.getContents());
//					((XMLDocumentProvider) getEditor().getDocumentProvider()).setContent(editor.getEditorInput(), new ByteArrayInputStream(sw.toString().getBytes()));
//					HibernateDocument hd = Synchronizer.getClasses((IFileEditorInput) editor.getEditorInput());
//					refresh(hd);
//					Synchronizer.synchronize(hd, JavaCore.create(file.getProject()), Display.getCurrent().getActiveShell());
//				}
//				catch (CoreException e) {
//					HSUtil.showError(e.getMessage(), new Shell());
//				}
			}
		}
	}

	public class ToggleNullableActionContribution extends ActionContributionItem {
		public ToggleNullableActionContribution (OutlinePage outlinePage) {
			super(new ToggleNullableAction(outlinePage));
		}
	}

	public class ToggleNullableAction extends Action {
		private OutlinePage outlinePage;
		public ToggleNullableAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setText("Toggle Nullable Status");
			this.setEnabled(true);
		}

		public void runWithEvent(Event event) {
			if (getEditor().isDirty()) {
				HSUtil.showError("You must save the document before performing this operation.", new Shell());
				return;
			}
			Tree tree = getTreeViewer().getTree();
			TreeItem[] items = tree.getSelection();
			Document doc = null;
			boolean changeMade = false;
			for (int i=0; i<items.length; i++) {
				TreeItem item = items[i];
				if (item.getData() instanceof HibernateClassProperty || item.getData() instanceof HibernateClassCollectionProperty) {
					Node node = ((BaseElement) item.getData()).getNode();
					if (null != node) {
						changeMade = true;
						doc = node.getOwnerDocument();
						NamedNodeMap nodeMap = node.getAttributes();
						Node att = nodeMap.getNamedItem("not-null");
						if (null != att) {
							if (att.getNodeValue().equals("true")) {
								att.setNodeValue("false");
							}
							else {
								att.setNodeValue("true");
							}
						}
						else {
							Attr a = node.getOwnerDocument().createAttribute("not-null");
							a.setNodeValue("true");
							((Element) node).setAttributeNode(a);
						}
					}
				}
			}
//			if (changeMade) {
//				try {
//					IFile file = ((FileEditorInput) editor.getEditorInput()).getFile();
//					StringWriter sw = new StringWriter();
//					XMLHelper xmlHelper = new XMLHelper(new PrintWriter(sw));
//					xmlHelper.printTree(doc, contentProvider.getDocument().getParser(), file.getContents());
//					((XMLDocumentProvider) getEditor().getDocumentProvider()).setContent(editor.getEditorInput(), new ByteArrayInputStream(sw.toString().getBytes()));
//					HibernateDocument hd = Synchronizer.getClasses((IFileEditorInput) editor.getEditorInput());
//					refresh(hd);
//				}
//				catch (Exception e) {
//					HSUtil.showError(e.getMessage(), new Shell());
//				}
//			}
		}
	}

	public class ShowManyToOneAction extends Action {
		private OutlinePage outlinePage;
		public ShowManyToOneAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setToolTipText("Show many-to-one nodes");
			this.setId("ShowManyToOne");
			this.setEnabled(true);
//			String value = ((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().getPersistentProperty(new QualifiedName("", getId()));
//			if (null != value) setChecked(true);
			setChecked(false);
			try {
				URL prefix = new URL(Plugin.getDefault().getDescriptor().getInstallURL(), "icons/");
				this.setImageDescriptor(ImageDescriptor.createFromURL(new URL(prefix, "nav_many_to_one.gif")));
			}
			catch (MalformedURLException e) {}
		}

		public void setChecked(boolean checked) {
			try {
				((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().setPersistentProperty(new QualifiedName("", getId()), new Boolean(checked).toString());
			}
			catch (Exception e) {
			}
			super.setChecked(checked);
			if (null != contentProvider) {
				contentProvider.m_showManyToOne = checked;
				contentProvider.refresh();
			}
			outlinePage.refresh();
			super.setChecked(checked);
		}
	}

	public class ShowOneToOneAction extends Action {
		private OutlinePage outlinePage;
		public ShowOneToOneAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setToolTipText("Show one-to-one nodes");
			this.setId("ShowOneToOne");
			this.setEnabled(true);
//			String value = ((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().getPersistentProperty(new QualifiedName("", getId()));
//			if (null != value) setChecked(true);
			setChecked(false);
			try {
				URL prefix = new URL(Plugin.getDefault().getDescriptor().getInstallURL(), "icons/");
				this.setImageDescriptor(ImageDescriptor.createFromURL(new URL(prefix, "nav_one_to_one.gif")));
			}
			catch (MalformedURLException e) {}
		}

		public void setChecked(boolean checked) {
			try {
				((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().setPersistentProperty(new QualifiedName("", getId()), new Boolean(checked).toString());
			}
			catch (Exception e) {
			}
			super.setChecked(checked);
			if (null != contentProvider) {
				contentProvider.m_showOneToOne = checked;
				contentProvider.refresh();
			}
			outlinePage.refresh();
			super.setChecked(checked);
		}
	}

	public class ShowPropertyAction extends Action {
		private OutlinePage outlinePage;
		public ShowPropertyAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setToolTipText("Show property nodes");
			this.setId("ShowProperty");
			this.setEnabled(true);
//			String value = ((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().getPersistentProperty(new QualifiedName("", getId()));
//			if (null != value) setChecked(true);
			setChecked(false);
			try {
				URL prefix = new URL(Plugin.getDefault().getDescriptor().getInstallURL(), "icons/");
				this.setImageDescriptor(ImageDescriptor.createFromURL(new URL(prefix, "nav_property.gif")));
			}
			catch (MalformedURLException e) {}
		}

		public void setChecked(boolean checked) {
			try {
				((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().setPersistentProperty(new QualifiedName("", getId()), new Boolean(checked).toString());
			}
			catch (Exception e) {
			}
			super.setChecked(checked);
			if (null != contentProvider) {
				contentProvider.m_showProperties = checked;
				contentProvider.refresh();
			}
			outlinePage.refresh();
			super.setChecked(checked);
		}
	}

	public class ShowQueryAction extends Action {
		private OutlinePage outlinePage;
		public ShowQueryAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setToolTipText("Show query nodes");
			this.setId("ShowQuery");
			this.setEnabled(true);
//			String value = ((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().getPersistentProperty(new QualifiedName("", getId()));
//			if (null != value) setChecked(true);
			setChecked(false);
			try {
				URL prefix = new URL(Plugin.getDefault().getDescriptor().getInstallURL(), "icons/");
				this.setImageDescriptor(ImageDescriptor.createFromURL(new URL(prefix, "nav_query.gif")));
			}
			catch (MalformedURLException e) {}
		}

		public void setChecked(boolean checked) {
			try {
				((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().setPersistentProperty(new QualifiedName("", getId()), new Boolean(checked).toString());
			}
			catch (Exception e) {
			}
			super.setChecked(checked);
			if (null != contentProvider) {
				contentProvider.m_showQueries = checked;
				contentProvider.refresh();
			}
			outlinePage.refresh();
			super.setChecked(checked);
		}
	}

	public class ShowCollectionAction extends Action {
		private OutlinePage outlinePage;
		public ShowCollectionAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setToolTipText("Show collection nodes");
			this.setId("ShowCollection");
			this.setEnabled(true);
//			String value = ((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().getPersistentProperty(new QualifiedName("", getId()));
//			if (null != value) setChecked(true);
			setChecked(false);
			try {
				URL prefix = new URL(Plugin.getDefault().getDescriptor().getInstallURL(), "icons/");
				this.setImageDescriptor(ImageDescriptor.createFromURL(new URL(prefix, "nav_list.gif")));
			}
			catch (MalformedURLException e) {}
		}

		public void setChecked(boolean checked) {
			try {
				((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().setPersistentProperty(new QualifiedName("", getId()), new Boolean(checked).toString());
			}
			catch (Exception e) {
			}
			super.setChecked(checked);
			if (null != contentProvider) {
				contentProvider.m_showCollections = checked;
				contentProvider.refresh();
			}
			outlinePage.refresh();
			super.setChecked(checked);
		}
	}

	public class ShowComponentAction extends Action {
		private OutlinePage outlinePage;
		public ShowComponentAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setToolTipText("Show component nodes");
			this.setId("ShowComponent");
			this.setEnabled(true);
//			String value = ((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().getPersistentProperty(new QualifiedName("", getId()));
//			if (null != value) setChecked(true);
			setChecked(false);
			try {
				URL prefix = new URL(Plugin.getDefault().getDescriptor().getInstallURL(), "icons/");
				this.setImageDescriptor(ImageDescriptor.createFromURL(new URL(prefix, "nav_component.gif")));
			}
			catch (MalformedURLException e) {}
		}

		public void setChecked(boolean checked) {
			try {
				((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().setPersistentProperty(new QualifiedName("", getId()), new Boolean(checked).toString());
			}
			catch (Exception e) {
			}
			super.setChecked(checked);
			if (null != contentProvider) {
				contentProvider.m_showComponents = checked;
				contentProvider.refresh();
			}
			outlinePage.refresh();
			super.setChecked(checked);
		}
	}

	public class ShowRequiredAction extends Action {
		private OutlinePage outlinePage;
		public ShowRequiredAction (OutlinePage outlinePage) {
			this.outlinePage = outlinePage;
			this.setToolTipText("Show only required nodes");
			this.setId("ShowRequired");
			this.setEnabled(true);
//			String value = ((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().getPersistentProperty(new QualifiedName("", getId()));
//			if (null != value) setChecked(true);
			setChecked(false);
			try {
				URL prefix = new URL(Plugin.getDefault().getDescriptor().getInstallURL(), "icons/");
				this.setImageDescriptor(ImageDescriptor.createFromURL(new URL(prefix, "nav_required.gif")));
			}
			catch (MalformedURLException e) {}
		}

		public void setChecked(boolean checked) {
			try {
				((FileEditorInput) outlinePage.getEditor().getEditorInput()).getFile().setPersistentProperty(new QualifiedName("", getId()), new Boolean(checked).toString());
			}
			catch (Exception e) {
				super.setChecked(true);
			}
			if (null != contentProvider) {
				contentProvider.showOnlyRequired = checked;
			}
			outlinePage.refresh();
			super.setChecked(checked);
		}
	}
}