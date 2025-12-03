package ui;

/**
 * 需要在 Tab 被选中时自动刷新的面板，实现该接口。
 */
public interface RefreshablePanel {
    /**
     * 从后端重新加载本面板所需的数据，并刷新界面。
     */
    void reloadData();
}
