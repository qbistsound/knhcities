package knh.cities;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
 
@Controller
public class controller
{
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    @RequestMapping(value = "/_cities", headers = "Accept=application/json")
    @ResponseBody
    public String _cities
    (
    	@RequestParam(name = "_bof", defaultValue = "1") String bof,
    	@RequestParam(name = "_size", defaultValue = "10") String page,
    	HttpServletRequest request
    ) 
    {
    	int index = (system.isnum(bof)) ? Integer.valueOf(bof).intValue() : 1;
    	int size = (system.isnum(page)) ? Integer.valueOf(page).intValue() : 10;
    	return system.database.records(index, size, ((request.isUserInRole("ROLE_ALLOW_EDIT")) ? true : false)).toString();
    }
    /*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    @RequestMapping(value = "/_search", headers = "Accept=application/json")
    @ResponseBody
    public String _search
    (
    	@RequestParam(name = "_bof", defaultValue = "1") String bof,
    	@RequestParam(name = "_size", defaultValue = "10") String page,
    	@RequestParam(name = "_query", defaultValue = "") String query,
    	HttpServletRequest request
    ) 
    {
    	int index = (system.isnum(bof)) ? Integer.valueOf(bof).intValue() : 1;
    	int size = (system.isnum(page)) ? Integer.valueOf(page).intValue() : 10;
    	Boolean mode = ((request.isUserInRole("ROLE_ALLOW_EDIT")) ? true : false);
    	return (!query.isEmpty()) ? system.database.search(query, size, mode).toString() : system.database.records(index, size, mode).toString();
    }
    /*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    @PreAuthorize("hasRole('ROLE_ALLOW_EDIT')")
    @RequestMapping(value = "/_upload", headers = "Accept=application/json")
    @ResponseBody
    public String _update
    (
    	@RequestParam("file") MultipartFile file,
    	@RequestParam(name = "_id", defaultValue = "1") String id,
    	@RequestParam(name = "_bof", defaultValue = "1") String bof,
        @RequestParam(name = "_size", defaultValue = "10") String page,
        HttpServletRequest request
    )
    {
    	if (system.isnum(id)) { try { system.database.image(Integer.valueOf(id).intValue(), file.getBytes()); } catch (Exception exception) { exception.printStackTrace(); } }
    	//
    	int index = (system.isnum(bof)) ? Integer.valueOf(bof).intValue() : 1;
    	int size = (system.isnum(page)) ? Integer.valueOf(page).intValue() : 10;
    	return system.database.records(index, size, ((request.isUserInRole("ROLE_ALLOW_EDIT")) ? true : false)).toString(); 
    }
    /*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    @PreAuthorize("hasRole('ROLE_ALLOW_EDIT')")
    @RequestMapping(value = "/_rename", headers = "Accept=application/json")
    @ResponseBody
    public String _rename
    (
    	@RequestParam(name = "_id", defaultValue = "1") String id,
    	@RequestParam(name = "_name", defaultValue = "") String name,
    	@RequestParam(name = "_bof", defaultValue = "1") String bof,
        @RequestParam(name = "_size", defaultValue = "10") String page,
        HttpServletRequest request
    )
    {
    	if (!name.isEmpty()) { system.database.rename(Integer.valueOf(id).intValue(), name); }
    	//
    	int index = (system.isnum(bof)) ? Integer.valueOf(bof).intValue() : 1;
    	int size = (system.isnum(page)) ? Integer.valueOf(page).intValue() : 10;
    	return system.database.records(index, size, ((request.isUserInRole("ROLE_ALLOW_EDIT")) ? true : false)).toString(); 
    }
    /*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
}