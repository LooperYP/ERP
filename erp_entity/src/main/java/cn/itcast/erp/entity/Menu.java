package cn.itcast.erp.entity;

import java.util.List;

/**
 * 菜单实体类
 * @author Administrator *
 */
public class Menu {	
	private String menuid;//菜单ID
	private String menuname;//菜单名称
	private String icon;//图标
	private String url;//URL
	//private String pid;//上级菜单ID
	private List<Menu> menus;

	public String getMenuid() {		
		return menuid;
	}
	public void setMenuid(String menuid) {
		this.menuid = menuid;
	}
	public String getMenuname() {		
		return menuname;
	}
	public void setMenuname(String menuname) {
		this.menuname = menuname;
	}
	public String getIcon() {		
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getUrl() {		
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<Menu> getMenus() {
		return menus;
	}
	public void setMenus(List<Menu> menus) {
		this.menus = menus;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((menuid == null) ? 0 : menuid.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Menu other = (Menu) obj;
		if (menuid == null) {
			if (other.menuid != null)
				return false;
		} else if (!menuid.equals(other.menuid))
			return false;
		return true;
	}
	
	
}
