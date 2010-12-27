jira-fields-with-permissions-plugin
============

This is a modified version of Balarami Reddy's Custom Fields with Security Levels Jira [plugin](http://confluence.atlassian.com/display/CODEGEIST/JIRA+Customfields+with+field+level+security).  It tackles a few issues I was having:

 * Our date and time format was not the same as the standard (dd.MM.yy vs. dd/MM/yyyy).  Having a hidden custom date field would throw a validation exception which the user could not see - thus not allowing the issue to be created.  This version of the code reads the date format from the application properties and uses this accordingly.
	
 * Custom field values were not being indexed after an issue was modified.  This version of the code explicitly calls the indexing service after a create or edit action.
	
 * When an issue was updated by a user who does not have view permission of a field, this field was subsequently ignored by the indexing service.  This version of the code recognises if the getValueFromIssue method is being called by the indexing service, and always returns the value if it is.
	


