::: Missing closing bracket
includes EEPROM;

module blabla
{
  provides interface StdControl;

  uses {
    interface StdControl as SubControl;
  }

implementation
{
  event int EEPROMWrite.writeDone(int success) {
    eepromLineInUse = FALSE;
    checkForSaveId();
    return SUCCESS;
  }

}